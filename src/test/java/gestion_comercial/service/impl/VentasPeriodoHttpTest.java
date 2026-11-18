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
import static gestion_comercial.entity.PeriodoVenta.DIA;
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
class VentasPeriodoHttpTest {
    private static final String URL = "/api/admin/ventas/periodo";
    private static final JsonMapper JSON = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build();
    private final String marca = "US47-" + UUID.randomUUID().toString().substring(0, 8);
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
        hoy = LocalDate.now();
        admin = usuario("ADMINISTRADOR");
    }

    @Test
    void zonaHorariaUsadaPorLosLimites() {
        assertEquals(hoy, LocalDate.now(ZoneId.systemDefault()));
        System.out.println("US47 JVM " + ZoneId.systemDefault() + " / " + hoy);
    }

    @ParameterizedTest
    @EnumSource(PeriodoVenta.class)
    void intervaloSemiabiertoYContratoExacto(PeriodoVenta periodo) throws Exception {
        LocalDate inicio = switch (periodo) {
            case DIA -> hoy;
            case SEMANA -> hoy.with(previousOrSame(DayOfWeek.MONDAY));
            case MES -> hoy.withDayOfMonth(1);
        };
        LocalDate fin = switch (periodo) {
            case DIA -> inicio.plusDays(1);
            case SEMANA -> inicio.plusWeeks(1);
            case MES -> inicio.plusMonths(1);
        };
        Totales base = consulta(periodo, inicio, fin);
        for (LocalDateTime fecha : List.of(inicio.atStartOfDay(), hoy.atTime(12, 0),
                fin.atStartOfDay().minusNanos(1_000))) {
            crear(fecha, CONFIRMADO, PAGADO, LOCAL, null, "0.11");
        }
        crear(inicio.atStartOfDay().minusNanos(1_000), CONFIRMADO, PAGADO, LOCAL, null, "1000");
        crear(fin.atStartOfDay(), CONFIRMADO, PAGADO, LOCAL, null, "10000");
        Totales resultado = consulta(periodo, inicio, fin);
        assertEquals(new Totales(3, dinero("0.33"), 3, dinero("0.33")), resultado.restar(base));

        assertEquals(resultado, jdbc.queryForObject("""
                SELECT COUNT(*), COALESCE(SUM(total), 0),
                       COUNT(*) FILTER (WHERE estado_pago = 'PAGADO'),
                       COALESCE(SUM(total) FILTER (WHERE estado_pago = 'PAGADO'), 0)
                FROM pedidos
                WHERE fecha_creacion >= ? AND fecha_creacion < ?
                  AND estado_pedido IN ('CONFIRMADO', 'EN_PREPARACION', 'LISTO_PARA_RETIRAR',
                                       'LISTO_PARA_ENVIAR', 'ENVIADO', 'ENTREGADO')
                """, (rs, row) -> new Totales(rs.getLong(1), rs.getBigDecimal(2).setScale(2),
                rs.getLong(3), rs.getBigDecimal(4).setScale(2)),
                inicio.atStartOfDay(), fin.atStartOfDay()));
    }

    @ParameterizedTest
    @EnumSource(EstadoPedido.class)
    void estadoDePedidoIndividual(EstadoPedido estado) throws Exception {
        Totales base = dia();
        crear(hoy.atTime(10, 0), estado, PAGADO, LOCAL, null, "12.34");
        boolean valido = estado != CANCELADO && estado != PENDIENTE_APROBACION;
        assertEquals(valido ? new Totales(1, dinero("12.34"), 1, dinero("12.34")) : Totales.CERO,
                dia().restar(base));
    }

    @Test
    void sinVentasValidasDevuelveCeros() throws Exception {
        Assumptions.assumeTrue(dia().equals(Totales.CERO), "Ya hay ventas válidas del día en la base de test");
        crear(hoy.atTime(8, 0), CANCELADO, PAGADO, LOCAL, null, "80.00");
        crear(hoy.atTime(8, 0), PENDIENTE_APROBACION, PAGADO, LOCAL, null, "90.00");
        assertEquals(Totales.CERO, dia());
    }

    @Test
    void cobrosCanalesConsumidorFinalDetallesSoloLecturaYTotalCero() throws Exception {
        Totales base = dia();
        var cliente = new Cliente();
        cliente.setNombre(marca);
        clientes.saveAndFlush(cliente);
        var online = crear(hoy.atTime(10, 0), CONFIRMADO, PENDIENTE, ONLINE, cliente, "10000");
        crear(hoy.atTime(11, 0), CONFIRMADO, PAGADO, LOCAL, null, "20000");
        crear(hoy.atTime(12, 0), CONFIRMADO, PAGADO, LOCAL, cliente, "30000");
        for (String sufijo : List.of("-A", "-B", "-C")) {
            var producto = new Producto();
            producto.setNombre(marca + sufijo);
            producto.setPrecioVenta(dinero("10"));
            productos.saveAndFlush(producto);
            var detalle = new DetallePedido();
            detalle.setPedido(online);
            detalle.setProducto(producto);
            detalle.setCantidad(1);
            detalle.setPrecioUnitario(dinero("10"));
            detalle.setSubtotal(dinero("10"));
            detalles.saveAndFlush(detalle);
        }
        var antes = snapshot();
        assertEquals(new Totales(3, dinero("60000"), 2, dinero("50000")), dia().restar(base));
        var lunes = hoy.with(previousOrSame(DayOfWeek.MONDAY));
        consulta(PeriodoVenta.SEMANA, lunes, lunes.plusWeeks(1));
        var primero = hoy.withDayOfMonth(1);
        consulta(PeriodoVenta.MES, primero, primero.plusMonths(1));
        assertEquals(antes, snapshot());
        crear(hoy.atTime(13, 0), CONFIRMADO, PAGADO, LOCAL, null, "0");
        assertEquals(new Totales(4, dinero("60000"), 3, dinero("50000")), dia().restar(base));
    }

    @Test
    void reflejaAltaCancelacionYTransicionDePago() throws Exception {
        Totales base = dia();
        var venta = crear(hoy.atTime(10, 0), CONFIRMADO, PAGADO, LOCAL, null, "17.36");
        assertEquals(new Totales(1, dinero("17.36"), 1, dinero("17.36")), dia().restar(base));
        venta.setEstadoPedido(CANCELADO);
        pedidos.saveAndFlush(venta);
        assertEquals(base, dia());
        var pendiente = crear(hoy.atTime(11, 0), CONFIRMADO, PENDIENTE, LOCAL, null, "43.11");
        assertEquals(new Totales(1, dinero("43.11"), 0, dinero("0")), dia().restar(base));
        pendiente.setEstadoPago(PAGADO);
        pendiente.setMedioPago(medio());
        pedidos.saveAndFlush(pendiente);
        assertEquals(new Totales(1, dinero("43.11"), 1, dinero("43.11")), dia().restar(base));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "?periodo=INVALIDO"})
    void parametroObligatorioYValido(String sufijo) throws Exception {
        mvc.perform(get(URL + sufijo).header("Authorization", "Bearer " + jwt.generateToken(admin)))
                .andExpect(status().isBadRequest());
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
        usuario.setPasswordHash("sin-login-us47");
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

    private Totales dia() throws Exception {
        return consulta(DIA, hoy, hoy.plusDays(1));
    }

    private Totales consulta(PeriodoVenta periodo, LocalDate inicio, LocalDate fin) throws Exception {
        var respuesta = mvc.perform(get(URL + "?periodo=" + periodo)
                        .header("Authorization", "Bearer " + jwt.generateToken(admin)))
                .andExpect(status().isOk()).andReturn().getResponse();
        var json = JSON.readTree(respuesta.getContentAsByteArray());
        assertEquals(Set.of("periodo", "fechaDesde", "fechaHasta", "cantidadVentasRealizadas",
                "montoTotalVendido", "cantidadVentasCobradas", "montoTotalCobrado"), json.propertyNames());
        assertEquals(periodo.name(), json.get("periodo").asText());
        assertEquals(inicio.toString(), json.get("fechaDesde").asText());
        assertEquals(fin.minusDays(1).toString(), json.get("fechaHasta").asText());
        assertTrue(json.get("cantidadVentasRealizadas").isIntegralNumber());
        assertTrue(json.get("cantidadVentasCobradas").isIntegralNumber());
        assertTrue(json.get("montoTotalVendido").isNumber());
        assertTrue(json.get("montoTotalCobrado").isNumber());
        return new Totales(json.get("cantidadVentasRealizadas").longValue(),
                dinero(json.get("montoTotalVendido").asText()), json.get("cantidadVentasCobradas").longValue(),
                dinero(json.get("montoTotalCobrado").asText()));
    }

    private static BigDecimal dinero(String valor) {
        return new BigDecimal(valor).setScale(2);
    }

    private record Totales(long realizadas, BigDecimal vendido, long cobradas, BigDecimal cobrado) {
        static final Totales CERO = new Totales(0, dinero("0"), 0, dinero("0"));

        Totales restar(Totales base) {
            return new Totales(realizadas - base.realizadas, vendido.subtract(base.vendido),
                    cobradas - base.cobradas, cobrado.subtract(base.cobrado));
        }
    }
}
