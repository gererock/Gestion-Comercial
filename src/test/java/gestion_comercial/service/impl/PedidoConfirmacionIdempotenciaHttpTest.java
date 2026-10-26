package gestion_comercial.service.impl;

import gestion_comercial.entity.*;
import gestion_comercial.repository.*;
import gestion_comercial.security.JwtService;
import gestion_comercial.support.TestDatabaseGuard;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Prueba HTTP real: las comprobaciones de estado se hacen después del commit/rollback del request. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseGuard.class)
class PedidoConfirmacionIdempotenciaHttpTest {
    @LocalServerPort int port;
    @Autowired ClienteRepository clientes;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired ProductoRepository productos;
    @Autowired MedioPagoRepository medios;
    @Autowired JwtService jwt;
    @Autowired JdbcTemplate jdbc;

    private record Comprador(Usuario usuario, Cliente cliente) {}
    private Comprador a, b;
    private Producto producto;
    private int medioPago;
    private final Set<UUID> claves = new HashSet<>();
    private final List<Integer> clientesCreados = new ArrayList<>();
    private final List<Integer> usuariosCreados = new ArrayList<>();

    @BeforeEach void preparar() {
        a = comprador();
        b = comprador();
        producto = new Producto();
        producto.setNombre("QA US25 idempotencia " + UUID.randomUUID());
        producto.setPrecioVenta(new BigDecimal("100.00"));
        producto.setStockActual(20);
        producto.setPublicadoOnline(true);
        producto = productos.saveAndFlush(producto);
        medioPago = medios.findAll().stream().filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .findFirst().orElseThrow().getIdMedioPago();
    }

    private Comprador comprador() {
        Usuario usuario = new Usuario();
        usuario.setNombre("QA US25 comprador");
        usuario.setEmail(UUID.randomUUID() + "@test.local");
        usuario.setRol(roles.findByNombre("CLIENTE").orElseThrow());
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setPasswordHash("no-utilizado-por-test-jwt");
        usuario.setFechaAlta(LocalDateTime.now());
        usuario = usuarios.saveAndFlush(usuario);
        usuariosCreados.add(usuario.getIdUsuario());
        Cliente cliente = new Cliente();
        cliente.setNombre("QA US25 cliente");
        cliente.setUsuario(usuario);
        cliente.setDescuentoPorcentaje(new BigDecimal("10"));
        cliente = clientes.saveAndFlush(cliente);
        clientesCreados.add(cliente.getIdCliente());
        return new Comprador(usuario, cliente);
    }

    private HttpResponse<String> post(String path, Usuario usuario, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + jwt.generateToken(usuario))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> online(Comprador comprador, UUID clave) throws Exception {
        claves.add(clave);
        String body = "{\"claveConfirmacion\":\"" + clave + "\",\"productos\":[{\"idProducto\":"
                + producto.getIdProducto() + ",\"cantidad\":2}],\"idMedioPago\":" + medioPago
                + ",\"medioEntrega\":\"RETIRO_LOCAL\"}";
        return post("/api/cliente/pedidos", comprador.usuario(), body);
    }

    private HttpResponse<String> local(Integer idCliente, UUID clave) throws Exception {
        claves.add(clave);
        String body = "{\"claveOperacion\":\"" + clave + "\",\"idCliente\":" + idCliente
                + ",\"items\":[{\"idProducto\":" + producto.getIdProducto() + ",\"cantidad\":2}]}";
        Usuario vendedor = usuarios.findByEmailIgnoreCase("vendedor@gestion.local").orElseThrow();
        return post("/api/vendedor/ventas-locales", vendedor, body);
    }

    private List<String> snapshot() {
        List<String> estado = new ArrayList<>();
        // Incluye contenido completo, no solo conteos: detecta modificaciones del pedido original.
        for (String tabla : List.of("pedidos", "detalle_pedido", "movimientos_stock", "reservas_stock")) {
            estado.add(jdbc.queryForObject("select coalesce(jsonb_agg(fila order by fila::text),'[]'::jsonb)::text "
                    + "from (select to_jsonb(t) fila from " + tabla + " t) s", String.class));
        }
        estado.add(jdbc.queryForObject("select row_to_json(p)::text from productos p where id_producto=?",
                String.class, producto.getIdProducto()));
        return estado;
    }

    private void conflictoSinCambios(Comprador comprador, UUID clave) throws Exception {
        List<String> antes = snapshot();
        HttpResponse<String> respuesta = online(comprador, clave);
        assertEquals(409, respuesta.statusCode(), respuesta.body());
        assertTrue(respuesta.body().contains("La confirmación indicada no es válida."));
        assertEquals(antes, snapshot(), "El rechazo no debe modificar pedido, detalles, movimientos, reservas ni stock");
    }

    @Test void onlineMismaClaveYMismoClienteEsReintentoValido() throws Exception {
        UUID clave = UUID.randomUUID();
        var original = online(a, clave);
        assertEquals(201, original.statusCode(), original.body());
        List<String> antes = snapshot();
        var repetida = online(a, clave);
        assertEquals(201, repetida.statusCode(), repetida.body());
        Integer id = jdbc.queryForObject("select id_pedido from pedidos where clave_confirmacion=?", Integer.class, clave);
        assertTrue(original.body().contains("\"idPedido\":" + id + ","));
        assertTrue(repetida.body().contains("\"idPedido\":" + id + ","));
        assertEquals(antes, snapshot());
    }

    @Test void localConClienteNoPuedeReutilizarseDesdeOnline() throws Exception {
        UUID clave = UUID.randomUUID();
        var original = local(a.cliente().getIdCliente(), clave);
        assertEquals(201, original.statusCode(), original.body());
        assertEquals("LOCAL", jdbc.queryForObject("select tipo_venta from pedidos where clave_confirmacion=?", String.class, clave));
        assertEquals(a.cliente().getIdCliente(), jdbc.queryForObject("select id_cliente from pedidos where clave_confirmacion=?", Integer.class, clave));
        conflictoSinCambios(a, clave);
    }

    @Test void localConsumidorFinalNoPuedeReutilizarseDesdeOnline() throws Exception {
        UUID clave = UUID.randomUUID();
        var original = local(null, clave);
        assertEquals(201, original.statusCode(), original.body());
        assertNull(jdbc.queryForObject("select id_cliente from pedidos where clave_confirmacion=?", Integer.class, clave));
        conflictoSinCambios(a, clave);
    }

    @Test void onlineDeClienteANoPuedeReutilizarsePorClienteB() throws Exception {
        UUID clave = UUID.randomUUID();
        var original = online(a, clave);
        assertEquals(201, original.statusCode(), original.body());
        conflictoSinCambios(b, clave);
    }

    @Test void claveNuevaCreaOnlineNormalConClienteYMedioPago() throws Exception {
        UUID clave = UUID.randomUUID();
        var respuesta = online(a, clave);
        assertEquals(201, respuesta.statusCode(), respuesta.body());
        assertEquals("ONLINE", jdbc.queryForObject("select tipo_venta from pedidos where clave_confirmacion=?", String.class, clave));
        assertEquals(a.cliente().getIdCliente(), jdbc.queryForObject("select id_cliente from pedidos where clave_confirmacion=?", Integer.class, clave));
        assertEquals(medioPago, jdbc.queryForObject("select id_medio_pago from pedidos where clave_confirmacion=?", Integer.class, clave));
        assertEquals(new BigDecimal("20.00"), jdbc.queryForObject("select descuento_cliente from pedidos where clave_confirmacion=?", BigDecimal.class, clave));
        assertEquals(new BigDecimal("180.00"), jdbc.queryForObject("select total from pedidos where clave_confirmacion=?", BigDecimal.class, clave));
        assertEquals(18, productos.findById(producto.getIdProducto()).orElseThrow().getStockActual());
    }

    @Test void onlineTampocoPuedeReutilizarseComoLocal() throws Exception {
        UUID clave = UUID.randomUUID();
        var original = online(a, clave);
        assertEquals(201, original.statusCode(), original.body());
        List<String> antes = snapshot();
        var respuesta = local(a.cliente().getIdCliente(), clave);
        assertEquals(409, respuesta.statusCode(), respuesta.body());
        assertEquals(antes, snapshot());
    }

    @AfterEach void limpiar() {
        if (producto != null && producto.getIdProducto() != null) {
            Integer id = producto.getIdProducto();
            jdbc.update("delete from reservas_stock where id_detalle_pedido in (select id_detalle_pedido from detalle_pedido where id_producto=?)", id);
            jdbc.update("delete from movimientos_stock where id_producto=?", id);
            jdbc.update("delete from detalle_pedido where id_producto=?", id);
        }
        for (UUID clave : claves) jdbc.update("delete from pedidos where clave_confirmacion=?", clave);
        if (producto != null && producto.getIdProducto() != null) productos.deleteById(producto.getIdProducto());
        clientesCreados.forEach(clientes::deleteById);
        usuariosCreados.forEach(usuarios::deleteById);
    }
}
