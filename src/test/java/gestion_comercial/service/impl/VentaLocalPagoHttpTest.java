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
class VentaLocalPagoHttpTest {
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

    private String body(UUID clave, Integer medio, String estado, String descripcion) {
        claves.add(clave);
        return "{\"claveOperacion\":\""+clave+"\",\"items\":[{\"idProducto\":"+producto.getIdProducto()+",\"cantidad\":2}],\"idMedioPago\":"+medio+",\"estadoPago\":"+(estado==null?"null":"\""+estado+"\"")+",\"descripcionPago\":"+(descripcion==null?"null":"\""+descripcion+"\"")+"}";
    }
    private HttpResponse<String> vender(String body) throws Exception {
        return post("/api/vendedor/ventas-locales", usuarios.findByEmailIgnoreCase("vendedor@gestion.local").orElseThrow(), body);
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"PAGADO","PENDIENTE"})
    void guardaPagoYNormalizaDescripcion(String estado) throws Exception {
        for (var medio : medios.findByActivoOrderByNombreAsc(true)) {
            UUID clave=UUID.randomUUID();
            var respuesta=vender(body(clave,medio.getIdMedioPago(),estado,"  Abona al retirar  "));
            assertEquals(201,respuesta.statusCode(),respuesta.body());
            assertEquals(medio.getIdMedioPago(),jdbc.queryForObject("select id_medio_pago from pedidos where clave_confirmacion=?",Integer.class,clave));
            assertEquals(estado,jdbc.queryForObject("select estado_pago from pedidos where clave_confirmacion=?",String.class,clave));
            assertEquals("Abona al retirar",jdbc.queryForObject("select descripcion_pago from pedidos where clave_confirmacion=?",String.class,clave));
            assertTrue(respuesta.body().contains(medio.getNombre()));
        }
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.NullAndEmptySource
    @org.junit.jupiter.params.provider.ValueSource(strings={"   ","@@@@"})
    void pendienteSinDescripcionSignificativaNoEscribe(String descripcion) throws Exception {
        var antes=snapshot();var r=vender(body(UUID.randomUUID(),medioPago,"PENDIENTE",descripcion));
        assertEquals(400,r.statusCode(),r.body());assertEquals(antes,snapshot());
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.NullAndEmptySource
    @org.junit.jupiter.params.provider.ValueSource(strings={"   "})
    void pagadoDescripcionOpcionalSeGuardaNull(String descripcion) throws Exception {
        UUID clave=UUID.randomUUID();var r=vender(body(clave,medioPago,"PAGADO",descripcion));assertEquals(201,r.statusCode(),r.body());
        assertNull(jdbc.queryForObject("select descripcion_pago from pedidos where clave_confirmacion=?",String.class,clave));
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(strings={"","PARCIAL","CUENTA_CORRIENTE","A_CONFIRMAR"})
    void estadoInvalidoNoEscribe(String estado) throws Exception {
        var antes=snapshot();var r=vender(body(UUID.randomUUID(),medioPago,estado,"Abona al retirar"));assertEquals(400,r.statusCode(),r.body());assertEquals(antes,snapshot());
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(ints={0,-1,2147483647})
    void medioInvalidoNoEscribe(Integer medio) throws Exception {
        var antes=snapshot();var r=vender(body(UUID.randomUUID(),medio,"PAGADO",null));assertEquals(400,r.statusCode(),r.body());assertEquals(antes,snapshot());
    }
    @Test void medioDesactivadoTrasCargaSeRechaza() throws Exception {
        MedioPago medio=new MedioPago();medio.setNombre("QA US26 "+UUID.randomUUID());medio.setActivo(true);medio=medios.saveAndFlush(medio);
        try {
            assertTrue(medios.findByActivoOrderByNombreAsc(true).stream().anyMatch(m->m.getNombre().startsWith("QA US26")));
            medio.setActivo(false);medios.saveAndFlush(medio);var antes=snapshot();
            var r=vender(body(UUID.randomUUID(),medio.getIdMedioPago(),"PAGADO",null));assertEquals(400,r.statusCode(),r.body());assertEquals(antes,snapshot());
        } finally {medios.deleteById(medio.getIdMedioPago());}
    }
    @Test void reintentoMismoPagoYTrimEsValidoYVariantesDanConflicto() throws Exception {
        UUID clave=UUID.randomUUID();assertEquals(201,vender(body(clave,medioPago,"PENDIENTE"," Abona al retirar ")).statusCode());var antes=snapshot();
        assertEquals(201,vender(body(clave,medioPago,"PENDIENTE","Abona al retirar")).statusCode());assertEquals(antes,snapshot());
        int otro=medios.findByActivoOrderByNombreAsc(true).stream().filter(m->m.getIdMedioPago()!=medioPago).findFirst().orElseThrow().getIdMedioPago();
        for(String body:List.of(body(clave,otro,"PENDIENTE","Abona al retirar"),body(clave,medioPago,"PAGADO","Abona al retirar"),body(clave,medioPago,"PENDIENTE","Abona mañana"))){
            var r=vender(body);assertEquals(409,r.statusCode(),r.body());assertEquals(antes,snapshot());
        }
    }
    @Test void listadoActivosYSoloRolesAutorizados() throws Exception {
        for(String email:List.of("vendedor@gestion.local","admin@gestion.local")){
            var req=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/vendedor/ventas-locales/medios-pago")).header("Authorization","Bearer "+jwt.generateToken(usuarios.findByEmailIgnoreCase(email).orElseThrow())).GET().build();
            var r=HttpClient.newHttpClient().send(req,HttpResponse.BodyHandlers.ofString());assertEquals(200,r.statusCode());assertTrue(r.body().contains("Efectivo"));assertTrue(r.body().contains("Transferencia"));assertFalse(r.body().contains("false"));
        }
        String body=body(UUID.randomUUID(),medioPago,"PAGADO",null);var antes=snapshot();
        assertEquals(403,post("/api/vendedor/ventas-locales",a.usuario(),body).statusCode());
        var req=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/vendedor/ventas-locales")).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        assertEquals(401,HttpClient.newHttpClient().send(req,HttpResponse.BodyHandlers.ofString()).statusCode());assertEquals(antes,snapshot());
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"PAGADO","PENDIENTE"})
    void clienteConPagoConservaDescuento(String estado) throws Exception {
        UUID clave=UUID.randomUUID();String contenido=body(clave,medioPago,estado,"Transferencia pendiente");
        contenido=contenido.replace("{\"claveOperacion", "{\"idCliente\":"+a.cliente().getIdCliente()+",\"claveOperacion");
        var r=vender(contenido);assertEquals(201,r.statusCode(),r.body());
        assertEquals(new BigDecimal("20.00"),jdbc.queryForObject("select descuento_cliente from pedidos where clave_confirmacion=?",BigDecimal.class,clave));
        assertEquals(new BigDecimal("180.00"),jdbc.queryForObject("select total from pedidos where clave_confirmacion=?",BigDecimal.class,clave));
    }
    @Test void sesionVencidaNoEscribe() throws Exception {
        String token=io.jsonwebtoken.Jwts.builder().subject("vendedor@gestion.local")
                .expiration(new Date(System.currentTimeMillis()-60000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("us24-test-only-secret-not-for-production-2026".getBytes(java.nio.charset.StandardCharsets.UTF_8))).compact();
        var antes=snapshot();var req=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/vendedor/ventas-locales"))
                .header("Authorization","Bearer "+token).header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body(UUID.randomUUID(),medioPago,"PAGADO",null))).build();
        assertEquals(401,HttpClient.newHttpClient().send(req,HttpResponse.BodyHandlers.ofString()).statusCode());assertEquals(antes,snapshot());
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
