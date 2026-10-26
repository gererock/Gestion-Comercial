package gestion_comercial.service.impl;

import gestion_comercial.entity.*;
import gestion_comercial.repository.*;
import gestion_comercial.security.JwtService;
import gestion_comercial.support.TestDatabaseGuard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseGuard.class)
class VentaLocalColisionHttpTest {
    @LocalServerPort int port;
    @Autowired ProductoRepository productos;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired JwtService jwt;
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean PedidoRepository pedidos;

    private String token(String email) {
        return jwt.generateToken(usuarios.findByEmailIgnoreCase(email).orElseThrow());
    }
    private String body(int producto, int cantidad, UUID clave) {
        return "{\"items\":[{\"idProducto\":" + producto + ",\"cantidad\":" + cantidad
                + "}],\"claveOperacion\":\"" + clave + "\"}";
    }
    private HttpResponse<String> post(String token, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/vendedor/ventas-locales"))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    private void limpiar(int producto, UUID clave) {
        jdbc.update("delete from movimientos_stock where id_producto=?", producto);
        jdbc.update("delete from detalle_pedido where id_pedido in (select id_pedido from pedidos where clave_confirmacion=?)", clave);
        jdbc.update("delete from pedidos where clave_confirmacion=?", clave);
        productos.deleteById(producto);
    }

    @Test
    void mismaClaveEntreResponsablesProduce201Y409ConRollbackYPermiteReintentoNormal() throws Exception {
        Producto p = new Producto();
        p.setNombre("QA US24 colisión " + UUID.randomUUID());
        p.setPrecioVenta(new BigDecimal("100.00"));
        p.setStockActual(10);
        p.setPublicadoOnline(false);
        p = productos.saveAndFlush(p);
        final int id = p.getIdProducto();
        UUID clave = UUID.randomUUID();
        String body = body(id, 2, clave);
        String vendedor = token("vendedor@gestion.local"), admin = token("admin@gestion.local");
        CyclicBarrier ambosConsultaron = new CyclicBarrier(2);
        // Fuerza la ventana real de carrera: ambos ven la clave todavía inexistente.
        // Persistencia, locks, UNIQUE, rollback y respuesta HTTP son reales.
        assertTrue(pedidos.findByClaveConfirmacion(clave).isEmpty());
        doAnswer(invocation -> {
            ambosConsultaron.await(20, TimeUnit.SECONDS);
            return Optional.empty();
        }).when(pedidos).findByClaveConfirmacion(clave);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<HttpResponse<String>> a = pool.submit(() -> post(vendedor, body));
            Future<HttpResponse<String>> b = pool.submit(() -> post(admin, body));
            var ra = a.get(30, TimeUnit.SECONDS);
            var rb = b.get(30, TimeUnit.SECONDS);
            assertEquals(List.of(201, 409), java.util.stream.Stream.of(ra.statusCode(), rb.statusCode()).sorted().toList());
            assertEquals(1, jdbc.queryForObject("select count(*) from pedidos where clave_confirmacion=?", Integer.class, clave));
            assertEquals(1, jdbc.queryForObject("select count(*) from detalle_pedido where id_producto=?", Integer.class, id));
            assertEquals(1, jdbc.queryForObject("select count(*) from movimientos_stock where id_producto=? and origen='VENTA_LOCAL'", Integer.class, id));
            assertEquals(8, productos.findById(id).orElseThrow().getStockActual());
            assertTrue((ra.statusCode() == 409 ? ra : rb).body().contains("La clave de operación ya fue utilizada."));
            reset(pedidos);
            String ganador = ra.statusCode() == 201 ? vendedor : admin;
            var original = ra.statusCode() == 201 ? ra : rb;
            var repetida = post(ganador, body);
            assertEquals(201, repetida.statusCode());
            assertEquals(original.body(), repetida.body());
            assertEquals(409, post(ganador, body(id, 3, clave)).statusCode());
            assertEquals(8, productos.findById(id).orElseThrow().getStockActual());
            assertEquals(1, jdbc.queryForObject("select count(*) from movimientos_stock where id_producto=?", Integer.class, id));
        } finally {
            pool.shutdownNow();
            pool.awaitTermination(30, TimeUnit.SECONDS);
            reset(pedidos);
            limpiar(id, clave);
        }
    }

    @Test
    void clienteRecibe403YVisitante401() throws Exception {
        Usuario cliente = new Usuario();
        cliente.setNombre("QA US24 cliente");
        cliente.setEmail("qa-us24-" + UUID.randomUUID() + "@test.local");
        cliente.setRol(roles.findByNombre("CLIENTE").orElseThrow());
        cliente.setEstado(EstadoUsuario.ACTIVO);
        cliente.setPasswordHash("no-utilizado-por-este-test-jwt");
        cliente.setFechaAlta(LocalDateTime.now());
        cliente = usuarios.saveAndFlush(cliente);
        try {
            String request = body(Integer.MAX_VALUE, 1, UUID.randomUUID());
            assertEquals(403, post(jwt.generateToken(cliente), request).statusCode());
            assertEquals(401, post(null, request).statusCode());
        } finally {
            usuarios.deleteById(cliente.getIdUsuario());
        }
    }

    @Test
    void onlineSinMedioPagoSigueRechazadoPorConstraint() {
        Integer cliente = jdbc.queryForObject("insert into clientes(nombre,estado) values ('QA US24 online',true) returning id_cliente", Integer.class);
        try {
            var error = assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                    insert into pedidos(numero_pedido,id_cliente,tipo_venta,estado_pedido,estado_pago,medio_entrega)
                    values (?,?,'ONLINE','PENDIENTE_APROBACION','PENDIENTE','RETIRO_LOCAL')
                    """, "QA-" + UUID.randomUUID().toString().substring(0,20), cliente));
            assertTrue(error.getMostSpecificCause().getMessage().contains("chk_pedido_medio_pago"));
        } finally {
            jdbc.update("delete from clientes where id_cliente=?", cliente);
        }
    }
}
