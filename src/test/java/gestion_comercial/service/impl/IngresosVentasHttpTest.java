package gestion_comercial.service.impl;

import gestion_comercial.entity.*;
import gestion_comercial.repository.*;
import gestion_comercial.security.JwtService;
import gestion_comercial.support.TestDatabaseGuard;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static gestion_comercial.entity.EstadoPago.PAGADO;
import static gestion_comercial.entity.EstadoPago.PENDIENTE;
import static gestion_comercial.entity.EstadoPedido.CONFIRMADO;
import static gestion_comercial.entity.EstadoPedido.CANCELADO;
import static gestion_comercial.entity.EstadoPedido.PENDIENTE_APROBACION;
import static gestion_comercial.entity.PeriodoIngreso.DIA;
import static gestion_comercial.entity.TipoVenta.LOCAL;
import static gestion_comercial.entity.TipoVenta.ONLINE;
import static java.time.temporal.TemporalAdjusters.previousOrSame;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseGuard.class)
@Transactional
class IngresosVentasHttpTest {
    private static final String URL = "/api/admin/ingresos-ventas";
    private static final JsonMapper JSON = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build();
    private final String marca = "US48-" + UUID.randomUUID().toString().substring(0, 8);
    private int secuencia;
    private LocalDate hoy;
    private Usuario admin;
    private MedioPago medioPago;

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired JwtService jwt;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired PedidoRepository pedidos;
    @Autowired DetallePedidoRepository detalles;
    @Autowired ProductoRepository productos;
    @Autowired ClienteRepository clientes;
    @Autowired MedioPagoRepository medios;
    @Value("${app.jwt.secret}") String jwtSecret;

    @BeforeEach
    void preparar() {
        TestDatabaseGuard.validarNombre(jdbc.queryForObject("select current_database()", String.class));
        hoy = LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));
        admin = usuario("ADMINISTRADOR");
    }

    @ParameterizedTest
    @ValueSource(strings = {"VENDEDOR", "CLIENTE"})
    void rolNoAutorizado(String rol) throws Exception {
        mvc.perform(get(URL + "?periodo=DIA").header("Authorization", "Bearer " + jwt.generateToken(usuario(rol))))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinJwt() throws Exception {
        mvc.perform(get(URL + "?periodo=DIA")).andExpect(status().isUnauthorized());
    }

    @Test
    void jwtInvalido() throws Exception {
        mvc.perform(get(URL + "?periodo=DIA").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtVencidoFirmadoConClaveDePrueba() throws Exception {
        var ahora = Instant.now();
        var token = Jwts.builder().subject(admin.getEmail()).claim("rol", "ADMINISTRADOR")
                .claim("idUsuario", admin.getIdUsuario()).issuedAt(Date.from(ahora.minusSeconds(120)))
                .expiration(Date.from(ahora.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8))).compact();
        mvc.perform(get(URL + "?periodo=DIA").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private Usuario usuario(String rol) {
        var usuario = new Usuario();
        usuario.setNombre(marca);
        usuario.setApellido(rol);
        usuario.setEmail(marca + rol + "@test.local");
        usuario.setRol(roles.findByNombre(rol).orElseThrow());
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setPasswordHash("sin-login-us48");
        usuario.setFechaAlta(hoy.atStartOfDay());
        return usuarios.saveAndFlush(usuario);
    }

    private Pedido crear(LocalDateTime fecha, EstadoPedido estado, EstadoPago pago, TipoVenta tipo,
                         Cliente cliente, String monto) {
        var pedido = new Pedido();
        pedido.setNumeroPedido(marca + "-" + ++secuencia);
        pedido.setFechaCreacion(fecha);
        pedido.setFechaActualizacion(fecha);
        pedido.setEstadoPedido(estado);
        pedido.setEstadoPago(pago);
        pedido.setTipoVenta(tipo);
        pedido.setCliente(cliente);
        if (tipo == ONLINE || pago == PAGADO) {
            pedido.setMedioPago(medio());
        }
        pedido.setMedioEntrega(MedioEntrega.RETIRO_LOCAL);
        pedido.setSubtotal(dinero(monto));
        pedido.setTotal(dinero(monto));
        return pedidos.saveAndFlush(pedido);
    }

    private MedioPago medio() {
        if (medioPago == null) {
            medioPago = medios.saveAndFlush(new MedioPago(null, marca, true));
        }
        return medioPago;
    }

    private List<Map<String, Object>> snapshot() {
        return jdbc.queryForList("""
                SELECT to_jsonb(p)::text AS pedido, to_jsonb(d)::text AS detalle
                FROM pedidos p LEFT JOIN detalle_pedido d USING (id_pedido)
                WHERE p.numero_pedido LIKE ?
                ORDER BY p.id_pedido, d.id_detalle_pedido
                """, marca + "-%");
    }

    @ParameterizedTest
    @EnumSource(value = PeriodoIngreso.class, names = {"DIA", "SEMANA", "MES"})
    void limitesYComparacionSql(PeriodoIngreso periodo) throws Exception {
        LocalDate inicio = switch (periodo) {
            case DIA -> hoy;
            case SEMANA -> hoy.with(previousOrSame(DayOfWeek.MONDAY));
            case MES -> hoy.withDayOfMonth(1);
            default -> throw new AssertionError(periodo);
        };
        LocalDate fin = switch (periodo) {
            case DIA -> inicio.plusDays(1);
            case SEMANA -> inicio.plusWeeks(1);
            case MES -> inicio.plusMonths(1);
            default -> throw new AssertionError(periodo);
        };
        String query = "?periodo=" + periodo;
        var base = consulta(query, periodo, inicio, fin.minusDays(1));
        for (var fecha : List.of(inicio.atStartOfDay(), hoy.atTime(8, 0), hoy.atTime(16, 0),
                fin.atStartOfDay().minusNanos(1000))) {
            crear(fecha, CONFIRMADO, PAGADO, LOCAL, null, "0.11");
        }
        crear(inicio.atStartOfDay().minusNanos(1000), CONFIRMADO, PAGADO, LOCAL, null, "1000");
        crear(fin.atStartOfDay(), CONFIRMADO, PAGADO, LOCAL, null, "10000");

        var actual = consulta(query, periodo, inicio, fin.minusDays(1));
        assertEquals(new Totales(4, dinero("0.44")), actual.restar(base));
        assertEquals(sql(inicio, fin), actual);
    }

    @ParameterizedTest
    @EnumSource(TipoVenta.class)
    void estadosYPagoEnAmbosCanales(TipoVenta tipo) throws Exception {
        var cliente = new Cliente();
        cliente.setNombre(marca);
        clientes.saveAndFlush(cliente);
        var previo = dia();
        for (EstadoPedido estado : EstadoPedido.values()) {
            String monto = estado == PENDIENTE_APROBACION ? "99999.99" : "10.25";
            crear(hoy.atTime(10, 0), estado, PAGADO, tipo, cliente, monto);
            var actual = dia();
            boolean excluido = estado == CANCELADO || estado == PENDIENTE_APROBACION;
            assertEquals(new Totales(excluido ? 0 : 1, dinero(excluido ? "0" : monto)), actual.restar(previo), estado.name());
            previo = actual;
        }
        crear(hoy.atTime(10, 0), CONFIRMADO, PENDIENTE, tipo, cliente, "10000");
        assertEquals(previo, dia());
    }

    @Test
    void altaPagoCancelacionYAprobacionSeReflejanEnCadaGet() throws Exception {
        var base = dia();
        var venta = crear(hoy.atTime(9, 0), CONFIRMADO, PAGADO, LOCAL, null, "20000");
        assertEquals(new Totales(1, dinero("20000")), dia().restar(base));
        var pendiente = crear(hoy.atTime(9, 0), CONFIRMADO, PENDIENTE, LOCAL, null, "10000");
        assertEquals(new Totales(1, dinero("20000")), dia().restar(base));
        pendiente.setMedioPago(medio());
        pendiente.setEstadoPago(PAGADO);
        pedidos.saveAndFlush(pendiente);
        assertEquals(new Totales(2, dinero("30000")), dia().restar(base));
        venta.setEstadoPedido(CANCELADO);
        pedidos.saveAndFlush(venta);
        assertEquals(new Totales(1, dinero("10000")), dia().restar(base));
        var porAprobar = crear(hoy.atTime(9, 0), PENDIENTE_APROBACION, PAGADO, LOCAL, null, "99999.99");
        assertEquals(new Totales(1, dinero("10000")), dia().restar(base));
        porAprobar.setEstadoPedido(CONFIRMADO);
        pedidos.saveAndFlush(porAprobar);
        assertEquals(new Totales(2, dinero("109999.99")), dia().restar(base));
    }

    @Test
    void rangoCerosPrecisionDetallesYConsultasSinMutacion() throws Exception {
        LocalDate inicio = hoy.plusYears(10);
        while (sql(inicio, inicio.plusDays(4)).cantidad() != 0) {
            inicio = inicio.plusDays(4);
        }
        LocalDate fin = inicio.plusDays(1);
        String fechas = "desde=" + inicio + "&hasta=" + fin;
        String rango = "?periodo=RANGO&" + fechas;
        var cero = new Totales(0, dinero("0"));
        assertEquals(cero, consulta(rango, PeriodoIngreso.RANGO, inicio, fin));
        crear(inicio.atStartOfDay(), CONFIRMADO, PENDIENTE, LOCAL, null, "10000");
        crear(inicio.atStartOfDay(), CANCELADO, PAGADO, LOCAL, null, "10000");
        crear(inicio.atStartOfDay(), PENDIENTE_APROBACION, PAGADO, LOCAL, null, "99999.99");
        assertEquals(cero, consulta(rango, PeriodoIngreso.RANGO, inicio, fin));

        var venta = crear(inicio.atStartOfDay(), CONFIRMADO, PAGADO, LOCAL, null, "10000.25");
        crear(fin.atStartOfDay(), CONFIRMADO, PAGADO, LOCAL, null, "20000.50");
        crear(fin.plusDays(1).atStartOfDay().minusNanos(1000), EstadoPedido.ENTREGADO, PAGADO, LOCAL, null, "30000.75");
        crear(inicio.minusDays(1).atTime(23, 59), CONFIRMADO, PAGADO, LOCAL, null, "100000");
        crear(fin.plusDays(1).atStartOfDay(), CONFIRMADO, PAGADO, LOCAL, null, "100000");

        for (String sufijo : List.of("-A", "-B", "-C")) {
            var producto = new Producto();
            producto.setNombre(marca + sufijo);
            producto.setPrecioVenta(dinero("10"));
            productos.saveAndFlush(producto);
            var detalle = new DetallePedido();
            detalle.setPedido(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(1);
            detalle.setPrecioUnitario(dinero("10"));
            detalle.setSubtotal(dinero("10"));
            detalles.saveAndFlush(detalle);
        }
        var pedidosAntes = snapshot();
        var stockAntes = stock();
        var esperado = new Totales(3, dinero("60001.50"));
        assertEquals(esperado, consulta(rango, PeriodoIngreso.RANGO, inicio, fin));
        assertEquals(esperado, consulta("?" + fechas, PeriodoIngreso.RANGO, inicio, fin));
        assertEquals(esperado, sql(inicio, fin.plusDays(1)));
        assertEquals(3L, jdbc.queryForObject("SELECT count(*) FROM detalle_pedido WHERE id_pedido = ?",
                Long.class, venta.getIdPedido()));
        assertEquals(new Totales(1, dinero("10000.25")),
                consulta("?periodo=RANGO&desde=" + inicio + "&hasta=" + inicio, PeriodoIngreso.RANGO, inicio, inicio));
        assertEquals(dia(), consulta("", DIA, hoy, hoy));
        LocalDate lunes = hoy.with(previousOrSame(DayOfWeek.MONDAY));
        consulta("?periodo=SEMANA", PeriodoIngreso.SEMANA, lunes, lunes.plusDays(6));
        consulta("?periodo=MES", PeriodoIngreso.MES, hoy.withDayOfMonth(1),
                hoy.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()));
        assertEquals(pedidosAntes, snapshot());
        assertEquals(stockAntes, stock());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "periodo=INVALIDO", "periodo=RANGO",
            "periodo=RANGO&desde={HOY}", "periodo=RANGO&hasta={HOY}",
            "periodo=RANGO&desde={HOY}&hasta={AYER}",
            "periodo=DIA&desde={HOY}&hasta={HOY}", "periodo=SEMANA&desde={HOY}&hasta={HOY}",
            "periodo=MES&desde={HOY}&hasta={HOY}", "desde={HOY}", "hasta={HOY}",
            "periodo=RANGO&desde=20-11-2026&hasta={HOY}"
    })
    void rechazaParametrosInvalidos(String parametros) throws Exception {
        var query = parametros.replace("{HOY}", hoy.toString()).replace("{AYER}", hoy.minusDays(1).toString());
        mvc.perform(get(URL + "?" + query).header("Authorization", "Bearer " + jwt.generateToken(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unaPendienteYDosPagadasSumanExactamente50000() throws Exception {
        var base = dia();
        crear(hoy.atTime(8, 0), CONFIRMADO, PENDIENTE, LOCAL, null, "10000");
        crear(hoy.atTime(9, 0), CONFIRMADO, PAGADO, LOCAL, null, "20000");
        crear(hoy.atTime(10, 0), EstadoPedido.ENTREGADO, PAGADO, LOCAL, null, "30000");
        assertEquals(new Totales(2, dinero("50000")), dia().restar(base));
    }

    private List<Map<String, Object>> stock() {
        return jdbc.queryForList("""
                SELECT 'producto' AS origen, to_jsonb(p)::text AS dato FROM productos p
                UNION ALL
                SELECT 'movimiento', to_jsonb(m)::text FROM movimientos_stock m
                ORDER BY origen, dato
                """);
    }

    private Totales sql(LocalDate inicio, LocalDate finExclusivo) {
        return jdbc.queryForObject("""
                SELECT count(*), coalesce(sum(total), 0)
                FROM pedidos
                WHERE fecha_creacion >= ? AND fecha_creacion < ?
                  AND estado_pago = 'PAGADO'
                  AND estado_pedido IN ('CONFIRMADO', 'EN_PREPARACION', 'LISTO_PARA_RETIRAR',
                                        'LISTO_PARA_ENVIAR', 'ENVIADO', 'ENTREGADO')
                """, (rs, fila) -> new Totales(rs.getLong(1), rs.getBigDecimal(2).setScale(2)),
                inicio.atStartOfDay(), finExclusivo.atStartOfDay());
    }

    private Totales dia() throws Exception {
        return consulta("?periodo=DIA", DIA, hoy, hoy);
    }

    private Totales consulta(String query, PeriodoIngreso periodo, LocalDate inicio, LocalDate fin) throws Exception {
        var respuesta = mvc.perform(get(URL + query).header("Authorization", "Bearer " + jwt.generateToken(admin)))
                .andExpect(status().isOk()).andReturn().getResponse();
        var json = JSON.readTree(respuesta.getContentAsByteArray());
        assertEquals(Set.of("periodo", "fechaDesde", "fechaHasta", "cantidadVentasCobradas",
                "montoTotalIngresos"), json.propertyNames());
        assertEquals(periodo.name(), json.get("periodo").asText());
        assertEquals(inicio.toString(), json.get("fechaDesde").asText());
        assertEquals(fin.toString(), json.get("fechaHasta").asText());
        assertTrue(json.get("cantidadVentasCobradas").isIntegralNumber());
        assertTrue(json.get("montoTotalIngresos").isNumber());
        return new Totales(json.get("cantidadVentasCobradas").longValue(), dinero(json.get("montoTotalIngresos").asText()));
    }

    private static BigDecimal dinero(String valor) {
        return new BigDecimal(valor).setScale(2);
    }

    private record Totales(long cantidad, BigDecimal monto) {
        Totales restar(Totales base) {
            return new Totales(cantidad - base.cantidad, monto.subtract(base.monto));
        }
    }
}


