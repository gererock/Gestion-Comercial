package gestion_comercial.service.impl;

import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.PeriodoIngreso;
import gestion_comercial.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class IngresosVentasServiceTest {
    private static final ZoneId CORDOBA = ZoneId.of("America/Argentina/Cordoba");
    private static final List<EstadoPedido> EXCLUIDOS =
            List.of(EstadoPedido.PENDIENTE_APROBACION, EstadoPedido.CANCELADO);

    private PedidoRepository pedidos;
    private IngresosVentasService servicio;

    @BeforeEach
    void preparar() {
        pedidos = mock(PedidoRepository.class);
        servicio = new IngresosVentasService(pedidos);
    }

    @ParameterizedTest
    @EnumSource(value = PeriodoIngreso.class, names = {"DIA", "SEMANA", "MES"})
    void consultaPeriodosCalendarioConLimitesSemiabiertos(PeriodoIngreso periodo) {
        LocalDate hoy = LocalDate.now(CORDOBA);
        LocalDate desde = switch (periodo) {
            case DIA -> hoy;
            case SEMANA -> hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MES -> hoy.withDayOfMonth(1);
            default -> throw new AssertionError(periodo);
        };
        LocalDate hasta = switch (periodo) {
            case DIA -> hoy;
            case SEMANA -> desde.plusDays(6);
            case MES -> hoy.with(TemporalAdjusters.lastDayOfMonth());
            default -> throw new AssertionError(periodo);
        };

        var reporte = servicio.consultarIngresos(periodo, null, null);

        assertThat(reporte.periodo()).isEqualTo(periodo);
        assertThat(reporte.fechaDesde()).isEqualTo(desde);
        assertThat(reporte.fechaHasta()).isEqualTo(hasta);
        assertThat(reporte.cantidadVentasCobradas()).isZero();
        assertThat(reporte.montoTotalIngresos()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(pedidos).buscarIngresosPorPeriodo(desde.atStartOfDay(),
                hasta.plusDays(1).atStartOfDay(), EstadoPago.PAGADO, EXCLUIDOS);
    }

    @Test
    void usaDiaPorDefecto() {
        assertThat(servicio.consultarIngresos(null, null, null))
                .isEqualTo(servicio.consultarIngresos(PeriodoIngreso.DIA, null, null));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void rangoExplicitoEImplicitoConPrecisionDecimal(int dias) {
        LocalDate desde = LocalDate.now(CORDOBA).minusDays(5);
        LocalDate hasta = desde.plusDays(dias);
        when(pedidos.buscarIngresosPorPeriodo(desde.atStartOfDay(),
                hasta.plusDays(1).atStartOfDay(), EstadoPago.PAGADO, EXCLUIDOS))
                .thenReturn(List.of(pedido("10000.25"), pedido("20000.50"), pedido("30000.75")));

        var reporte = servicio.consultarIngresos(PeriodoIngreso.RANGO, desde, hasta);

        assertThat(reporte.periodo()).isEqualTo(PeriodoIngreso.RANGO);
        assertThat(reporte.fechaDesde()).isEqualTo(desde);
        assertThat(reporte.fechaHasta()).isEqualTo(hasta);
        assertThat(reporte.cantidadVentasCobradas()).isEqualTo(3);
        assertThat(reporte.montoTotalIngresos()).isEqualByComparingTo("60001.50");
        assertThat(servicio.consultarIngresos(null, desde, hasta)).isEqualTo(reporte);
    }

    @ParameterizedTest
    @EnumSource(value = PeriodoIngreso.class, names = {"DIA", "SEMANA", "MES"})
    void rechazaFechasManualesEnPeriodosCalendario(PeriodoIngreso periodo) {
        LocalDate hoy = LocalDate.now(CORDOBA);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> servicio.consultarIngresos(periodo, hoy, hoy));
        verifyNoInteractions(pedidos);
    }

    @Test
    void rechazaRangosIncompletosOInvertidosAntesDeConsultar() {
        LocalDate hoy = LocalDate.now(CORDOBA);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> servicio.consultarIngresos(PeriodoIngreso.RANGO, null, null));
        for (PeriodoIngreso periodo : new PeriodoIngreso[]{PeriodoIngreso.RANGO, null}) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> servicio.consultarIngresos(periodo, hoy, null));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> servicio.consultarIngresos(periodo, null, hoy));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> servicio.consultarIngresos(periodo, hoy, hoy.minusDays(1)));
        }
        verifyNoInteractions(pedidos);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2001-02-28", "2000-02-29", "2000-04-30", "2000-01-31"})
    void mesCalendarioDe28_29_30_31Dias(String ultimoDia) {
        LocalDate hasta = LocalDate.parse(ultimoDia);
        LocalDate desde = hasta.withDayOfMonth(1);
        LocalDate hoy = hasta.withDayOfMonth(15);
        var inicio = desde.atStartOfDay();
        var fin = hasta.plusDays(1).atStartOfDay();

        try (var fechas = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            fechas.when(() -> LocalDate.now(CORDOBA)).thenReturn(hoy);
            var reporte = servicio.consultarIngresos(PeriodoIngreso.MES, null, null);

            assertThat(reporte.fechaDesde()).isEqualTo(desde);
            assertThat(reporte.fechaHasta()).isEqualTo(hasta);
            verify(pedidos).buscarIngresosPorPeriodo(inicio, fin, EstadoPago.PAGADO, EXCLUIDOS);
        }
    }

    private static Pedido pedido(String total) {
        Pedido pedido = new Pedido();
        pedido.setTotal(new BigDecimal(total));
        return pedido;
    }
}

