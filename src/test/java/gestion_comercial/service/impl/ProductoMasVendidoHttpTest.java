package gestion_comercial.service.impl;

import gestion_comercial.entity.*;
import gestion_comercial.repository.*;
import gestion_comercial.security.JwtService;
import gestion_comercial.support.TestDatabaseGuard;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static gestion_comercial.entity.EstadoPedido.*;
import static gestion_comercial.entity.EstadoPago.*;
import static gestion_comercial.entity.TipoVenta.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false, print = org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint.NONE)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseGuard.class)
@Transactional
class ProductoMasVendidoHttpTest {
    private static final String URL = "/api/admin/productos-mas-vendidos";
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final String prefijo = "US50-" + UUID.randomUUID().toString().substring(0, 8);
    private final LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));
    private int secuencia;
    private Usuario admin;

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate sql;
    @Autowired JwtService jwt;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired PedidoRepository pedidos;
    @Autowired DetallePedidoRepository detalles;
    @Autowired ProductoRepository productos;
    @Autowired ClienteRepository clientes;
    @Autowired MedioPagoRepository medios;
    @Value("${app.jwt.secret}") String claveJwt;

    @BeforeEach
    void preparar() {
        TestDatabaseGuard.validarNombre(sql.queryForObject("select current_database()", String.class));
        admin = usuario("ADMINISTRADOR");
    }

    @Test
    void rankingCompletoContraSqlEHistoricos() throws Exception {
        var a = producto("A", EstadoProducto.ACTIVO);
        var b = producto("B", EstadoProducto.ACTIVO);
        var c = producto("C", EstadoProducto.ACTIVO);
        var d = producto("D", EstadoProducto.PAUSADO);
        var e = producto("E", EstadoProducto.DISCONTINUADO);
        var f = producto("F", EstadoProducto.ACTIVO);
        var fecha = hoy.atTime(12, 0);
        var online = pedido(fecha, CONFIRMADO, PENDIENTE, ONLINE);
        linea(online, a, 70, "120.00");
        linea(online, b, 60, "300.00");
        linea(pedido(fecha, EN_PREPARACION, PAGADO, ONLINE), a, 20, "70.00");
        var local = pedido(fecha, LISTO_PARA_RETIRAR, PAGADO, LOCAL);
        linea(local, a, 10, "35.00");
        linea(local, c, 90, "50.00");
        linea(pedido(fecha, LISTO_PARA_ENVIAR, PENDIENTE, LOCAL), b, 20, "120.00");
        linea(pedido(fecha, CANCELADO, PAGADO, LOCAL), a, 999, "9999.00");
        linea(pedido(fecha, CANCELADO, PAGADO, LOCAL), f, 999, "9999.00");
        linea(pedido(fecha, PENDIENTE_APROBACION, PENDIENTE, ONLINE), b, 999, "9999.00");
        linea(pedido(fecha, ENVIADO, PENDIENTE, ONLINE), d, 3, "12.50");
        linea(pedido(fecha, ENTREGADO, PAGADO, LOCAL), e, 3, "9.00");
        var antes = huellaNegocio();
        var resultado = consultar("?periodo=DIA");
        contrastar(resultado, hoy, hoy.plusDays(1));
        assertEquals(antes, huellaNegocio());
        assertEquals(List.of(a.getIdProducto(), c.getIdProducto(), b.getIdProducto(),
                        d.getIdProducto(), e.getIdProducto()),
                idsQA(resultado));
        assertEquals(100L, resultado.get(0).get("cantidadVendida").longValue());
        assertEquals(0, new BigDecimal("225.00").compareTo(resultado.get(0).get("totalVendido").decimalValue()));
        a.setPrecioVenta(new BigDecimal("950.00"));
        productos.saveAndFlush(a);
        assertEquals(resultado, consultar("?periodo=DIA"));
        assertEquals(resultado, consultar(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DIA", "SEMANA", "MES", "RANGO"})
    void limitesDelPeriodoContraSql(String periodo) throws Exception {
        LocalDate inicio = switch (periodo) {
            case "SEMANA" -> hoy.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case "MES" -> hoy.withDayOfMonth(1);
            default -> hoy;
        };
        LocalDate fin = switch (periodo) {
            case "SEMANA" -> inicio.plusWeeks(1);
            case "MES" -> inicio.plusMonths(1);
            case "RANGO" -> inicio.plusDays(2);
            default -> inicio.plusDays(1);
        };
        var producto = producto("limites", EstadoProducto.ACTIVO);
        for (var fecha : List.of(inicio.atStartOfDay(), hoy.atTime(12, 0), fin.atStartOfDay().minusNanos(1000))) {
            linea(pedido(fecha, CONFIRMADO, PENDIENTE, LOCAL), producto, 1, "2.25");
        }
        for (var fecha : List.of(inicio.atStartOfDay().minusNanos(1000), fin.atStartOfDay())) {
            linea(pedido(fecha, CONFIRMADO, PENDIENTE, LOCAL), producto, 1000, "3000.00");
        }
        String fechas = "&desde=" + inicio + "&hasta=" + fin.minusDays(1);
        var resultado = consultar("?periodo=" + periodo + (periodo.equals("RANGO") ? fechas : ""));
        contrastar(resultado, inicio, fin);
        for (var fila : resultado) {
            if (fila.get("idProducto").intValue() == producto.getIdProducto()) {
                assertEquals(3, fila.get("cantidadVendida").intValue());
            }
        }
        if (periodo.equals("RANGO")) {
            assertEquals(resultado, consultar("?" + fechas.substring(1)));
        }
    }

    @Test
    void rangoDeUnDiaYListaVacia() throws Exception {
        assertEquals(consultar("?periodo=DIA"), consultar("?periodo=RANGO&desde=" + hoy + "&hasta=" + hoy));
        assertTrue(consultar("?periodo=RANGO&desde=1900-01-01&hasta=1900-01-01").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "periodo=RANGO&desde=2026-11-27&hasta=2026-11-26",
        "periodo=RANGO&hasta=2026-11-26", "periodo=RANGO&desde=2026-11-26",
        "periodo=RANGO&desde=2026-02-30&hasta=2026-11-26",
        "periodo=RANGO&desde=26/11/2026&hasta=2026-11-26",
        "periodo=RANGO&desde=hola&hasta=2026-11-26",
        "periodo=MES&desde=2026-11-26&hasta=2026-11-26"
    })
    void parametrosInvalidos(String parametros) throws Exception {
        mvc.perform(get(URL + "?" + parametros).header("Authorization", "Bearer " + jwt.generateToken(admin)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"VENDEDOR", "CLIENTE"})
    void rolSinAcceso(String rol) throws Exception {
        mvc.perform(get(URL).header("Authorization", "Bearer " + jwt.generateToken(usuario(rol))))
                .andExpect(status().isForbidden());
    }

    @Test
    void credencialesAusentesInvalidasOVencidas() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mvc.perform(get(URL).header("Authorization", "Bearer invalido")).andExpect(status().isUnauthorized());
        var ahora = Instant.now();
        var vencido = Jwts.builder().subject(admin.getEmail()).claim("rol", "ADMINISTRADOR")
                .claim("idUsuario", admin.getIdUsuario()).issuedAt(Date.from(ahora.minusSeconds(120)))
                .expiration(Date.from(ahora.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(claveJwt.getBytes(StandardCharsets.UTF_8))).compact();
        mvc.perform(get(URL).header("Authorization", "Bearer " + vencido)).andExpect(status().isUnauthorized());
    }

    @Test
    void ventaLocalPorEndpointActualizaElRanking() throws Exception {
        var a = producto("dinamico", EstadoProducto.ACTIVO);
        a.setStockActual(20);
        productos.saveAndFlush(a);
        var medio = medios.saveAndFlush(new MedioPago(null, prefijo, true));
        var vendedor = usuario("VENDEDOR");
        assertFalse(idsQA(consultar("?periodo=DIA")).contains(a.getIdProducto()));
        var clave = UUID.randomUUID();
        var body = """
                {"claveOperacion":"%s","items":[{"idProducto":%d,"cantidad":2}],
                 "idMedioPago":%d,"estadoPago":"PAGADO"}
                """.formatted(clave, a.getIdProducto(), medio.getIdMedioPago());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/vendedor/ventas-locales").contentType("application/json").content(body)
                        .header("Authorization", "Bearer " + jwt.generateToken(vendedor)))
                .andExpect(status().isCreated());
        assertEquals(2, sql.queryForObject("""
                SELECT d.cantidad FROM detalle_pedido d JOIN pedidos p USING(id_pedido)
                WHERE clave_confirmacion = ? AND tipo_venta = 'LOCAL'
                """, Integer.class, clave));
        var resultado = consultar("?periodo=DIA");
        assertEquals(List.of(a.getIdProducto()), idsQA(resultado));
        contrastar(resultado, hoy, hoy.plusDays(1));
    }

    @Test
    void flujoOnlineRealYCancelacionControladaSeReflejanEnElRanking() throws Exception {
        var a = producto("transicion", EstadoProducto.ACTIVO);
        a.setStockActual(20);
        productos.saveAndFlush(a);
        var comprador = usuario("CLIENTE");
        var c = new Cliente();
        c.setNombre(prefijo);
        c.setUsuario(comprador);
        clientes.saveAndFlush(c);
        var medio = medios.saveAndFlush(new MedioPago(null, prefijo, true));
        var clave = UUID.randomUUID();
        var body = """
                {"claveConfirmacion":"%s","productos":[{"idProducto":%d,"cantidad":7}],
                 "idMedioPago":%d,"medioEntrega":"RETIRO_LOCAL"}
                """.formatted(clave, a.getIdProducto(), medio.getIdMedioPago());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/cliente/pedidos").contentType("application/json").content(body)
                        .header("Authorization", "Bearer " + jwt.generateToken(comprador)))
                .andExpect(status().isCreated());
        int id = sql.queryForObject("SELECT id_pedido FROM pedidos WHERE clave_confirmacion = ?", Integer.class, clave);
        assertEquals("PENDIENTE_APROBACION", estadoPedido(id));
        assertTrue(idsQA(consultar("?periodo=DIA")).isEmpty());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/vendedor/pedidos/{id}/aprobar", id)
                        .header("Authorization", "Bearer " + jwt.generateToken(admin)))
                .andExpect(status().isOk());
        entidades.flush();
        assertEquals("CONFIRMADO", estadoPedido(id));
        var confirmado = consultar("?periodo=DIA");
        contrastar(confirmado, hoy, hoy.plusDays(1));
        assertEquals(List.of(a.getIdProducto()), idsQA(confirmado));

        // El endpoint de cancelación solo admite pendientes de aprobación.
        assertEquals(1, sql.update("UPDATE pedidos SET estado_pedido = 'CANCELADO' WHERE id_pedido = ?", id));
        entidades.clear();
        assertEquals("CANCELADO", estadoPedido(id));
        var cancelado = consultar("?periodo=DIA");
        contrastar(cancelado, hoy, hoy.plusDays(1));
        assertTrue(idsQA(cancelado).isEmpty());
    }

    private String estadoPedido(int id) {
        return sql.queryForObject("SELECT estado_pedido FROM pedidos WHERE id_pedido = ?", String.class, id);
    }

    @Test
    void regresionComparacionYTicketContraSql() throws Exception {
        var inicio = LocalDate.of(2001, 11, 19);
        var fin = inicio.plusDays(2);
        pedido(inicio.atStartOfDay(), CONFIRMADO, PENDIENTE, ONLINE);
        pedido(fin.atStartOfDay().minusNanos(1000), ENTREGADO, PAGADO, LOCAL);
        pedido(inicio.atTime(12, 0), CANCELADO, PAGADO, LOCAL);
        pedido(inicio.atTime(15, 0), PENDIENTE_APROBACION, PENDIENTE, ONLINE);
        pedido(fin.atStartOfDay(), CONFIRMADO, PAGADO, ONLINE);
        String fechas = "?periodo=RANGO&desde=" + inicio + "&hasta=" + fin.minusDays(1);
        var antes = huellaNegocio();
        var comparacion = consultarReporte("/api/admin/comparacion-ventas" + fechas);
        var ticket = consultarReporte("/api/admin/ticket-promedio" + fechas);
        assertEquals(inicio.toString(), comparacion.get("fechaDesde").asText());
        assertEquals(fin.minusDays(1).toString(), ticket.get("fechaHasta").asText());
        var sqlVentas = sql.queryForMap("""
                SELECT count(*) FILTER (WHERE tipo_venta='ONLINE') AS n_online,
                       sum(total) FILTER (WHERE tipo_venta='ONLINE') AS m_online,
                       count(*) FILTER (WHERE tipo_venta='LOCAL') AS n_local,
                       sum(total) FILTER (WHERE tipo_venta='LOCAL') AS m_local,
                       count(*) AS cantidad, sum(total) AS total, avg(total) AS promedio
                  FROM pedidos
                 WHERE fecha_creacion >= ? AND fecha_creacion < ?
                   AND estado_pedido NOT IN ('CANCELADO','PENDIENTE_APROBACION')
                """, inicio.atStartOfDay(), fin.atStartOfDay());
        assertEquals(((Number) sqlVentas.get("n_online")).longValue(), comparacion.get("cantidadVentasOnline").longValue());
        assertEquals(((Number) sqlVentas.get("n_local")).longValue(), comparacion.get("cantidadVentasLocales").longValue());
        assertEquals(((Number) sqlVentas.get("cantidad")).longValue(), ticket.get("cantidadVentas").longValue());
        assertDinero(sqlVentas, "m_online", comparacion, "montoTotalOnline");
        assertDinero(sqlVentas, "m_local", comparacion, "montoTotalLocal");
        assertDinero(sqlVentas, "total", ticket, "totalVendido");
        assertDinero(sqlVentas, "promedio", ticket, "ticketPromedio");
        assertEquals(antes, huellaNegocio());
    }

    private static void assertDinero(Map<String, Object> valores, String clave, JsonNode json, String campo) {
        assertEquals(0, ((BigDecimal) valores.get(clave)).compareTo(json.get(campo).decimalValue()));
    }

    private JsonNode consultarReporte(String ruta) throws Exception {
        var response = mvc.perform(get(ruta).header("Authorization", "Bearer " + jwt.generateToken(admin)))
                .andExpect(status().isOk()).andReturn().getResponse();
        return JSON.readTree(response.getContentAsByteArray());
    }
    private JsonNode consultar(String sufijo) throws Exception {
        var response = mvc.perform(get(URL + sufijo).header("Authorization", "Bearer " + jwt.generateToken(admin)))
                .andExpect(status().isOk()).andReturn().getResponse();
        var body = JSON.readTree(response.getContentAsByteArray());
        assertTrue(body.isArray());
        return body;
    }

    private void contrastar(JsonNode resultado, LocalDate inicio, LocalDate fin) {
        var esperado = sql.query("""
                SELECT p.id_producto, p.nombre, c.nombre, m.nombre, SUM(d.cantidad), SUM(d.subtotal)
                  FROM detalle_pedido d JOIN pedidos o USING (id_pedido) JOIN productos p USING (id_producto)
                  LEFT JOIN categorias c USING (id_categoria) LEFT JOIN marcas m USING (id_marca)
                 WHERE o.fecha_creacion >= ? AND o.fecha_creacion < ?
                   AND o.estado_pedido IN ('CONFIRMADO','EN_PREPARACION','LISTO_PARA_RETIRAR',
                                           'LISTO_PARA_ENVIAR','ENVIADO','ENTREGADO')
                 GROUP BY p.id_producto, c.nombre, m.nombre
                 ORDER BY SUM(d.cantidad) DESC, p.id_producto
                """, (rs, i) -> new Fila(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getLong(5), rs.getBigDecimal(6).stripTrailingZeros()),
                inicio.atStartOfDay(), fin.atStartOfDay());
        var observado = new java.util.ArrayList<Fila>();
        for (var fila : resultado) {
            assertEquals(Set.of("idProducto", "nombre", "categoria", "marca", "cantidadVendida", "totalVendido"),
                    fila.propertyNames());
            observado.add(new Fila(fila.get("idProducto").intValue(), texto(fila.get("nombre")),
                    texto(fila.get("categoria")), texto(fila.get("marca")), fila.get("cantidadVendida").longValue(),
                    fila.get("totalVendido").decimalValue().stripTrailingZeros()));
        }
        assertEquals(esperado, observado);
    }

    private List<Map<String, Object>> huellaNegocio() {
        return sql.queryForList("""
                SELECT 'productos' AS tabla, md5(COALESCE(string_agg(to_jsonb(p)::text, '' ORDER BY id_producto), '')) FROM productos p
                UNION ALL
                SELECT 'pedidos', md5(COALESCE(string_agg(to_jsonb(o)::text, '' ORDER BY id_pedido), '')) FROM pedidos o
                UNION ALL
                SELECT 'detalle_pedido', md5(COALESCE(string_agg(to_jsonb(d)::text, '' ORDER BY id_detalle_pedido), '')) FROM detalle_pedido d
                UNION ALL
                SELECT 'movimientos_stock', md5(COALESCE(string_agg(to_jsonb(m)::text, '' ORDER BY to_jsonb(m)::text), '')) FROM movimientos_stock m
                """);
    }

    private List<Integer> idsQA(JsonNode resultado) {
        var ids = new java.util.ArrayList<Integer>();
        for (var fila : resultado) {
            if (fila.get("nombre").asText().startsWith(prefijo)) {
                ids.add(fila.get("idProducto").intValue());
            }
        }
        return ids;
    }

    private static String texto(JsonNode dato) {
        return dato.isNull() ? null : dato.asText();
    }

    private Producto producto(String nombre, EstadoProducto estado) {
        var p = new Producto();
        p.setNombre(prefijo + "-" + nombre);
        p.setEstado(estado);
        p.setPrecioVenta(new BigDecimal("20.00"));
        if ("A".equals(nombre) || "B".equals(nombre)) {
            var categoria = new Categoria();
            categoria.setNombre(prefijo + "-cat-" + nombre);
            entidades.persist(categoria);
            p.setCategoria(categoria);
            var marca = new Marca();
            marca.setNombre(prefijo + "-marca-" + nombre);
            entidades.persist(marca);
            p.setMarca(marca);
        }
        return productos.saveAndFlush(p);
    }

    @Autowired jakarta.persistence.EntityManager entidades;

    private Usuario usuario(String nombreRol) {
        var u = new Usuario();
        u.setNombre(prefijo);
        u.setApellido(nombreRol);
        u.setEmail(prefijo + nombreRol + "@test.local");
        u.setRol(roles.findByNombre(nombreRol).orElseThrow());
        u.setEstado(EstadoUsuario.ACTIVO);
        u.setPasswordHash("fixture-sin-login");
        u.setFechaAlta(hoy.atStartOfDay());
        return usuarios.saveAndFlush(u);
    }

    private Pedido pedido(LocalDateTime fecha, EstadoPedido estado, EstadoPago pago, TipoVenta tipo) {
        var p = new Pedido();
        p.setNumeroPedido(prefijo + "-" + ++secuencia);
        p.setFechaCreacion(fecha);
        p.setFechaActualizacion(fecha);
        p.setEstadoPedido(estado);
        p.setEstadoPago(pago);
        p.setTipoVenta(tipo);
        p.setMedioEntrega(MedioEntrega.RETIRO_LOCAL);
        if (tipo == ONLINE) {
            var c = new Cliente();
            c.setNombre(prefijo);
            p.setCliente(clientes.saveAndFlush(c));
        }
        if (tipo == ONLINE || pago == PAGADO) {
            p.setMedioPago(medios.saveAndFlush(new MedioPago(null, prefijo + "-" + secuencia, true)));
        }
        p.setSubtotal(new BigDecimal("10000.00"));
        p.setTotal(new BigDecimal("10000.00"));
        return pedidos.saveAndFlush(p);
    }

    private void linea(Pedido pedido, Producto producto, int cantidad, String subtotal) {
        var d = new DetallePedido();
        d.setPedido(pedido);
        d.setProducto(producto);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(new BigDecimal("30.00"));
        d.setDescuentoAplicado(BigDecimal.ONE);
        d.setSubtotal(new BigDecimal(subtotal));
        detalles.saveAndFlush(d);
    }

    private record Fila(int id, String nombre, String categoria, String marca, long cantidad, BigDecimal total) {}
}



