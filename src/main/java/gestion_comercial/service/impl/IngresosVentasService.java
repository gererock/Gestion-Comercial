package gestion_comercial.service.impl;

import gestion_comercial.dto.response.IngresosVentasResponse;
import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.PeriodoIngreso;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.service.interfaces.IIngresosVentasService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;


@Service
@RequiredArgsConstructor
public class IngresosVentasService
        implements IIngresosVentasService {


    private static final ZoneId ZONA_HORARIA =
            ZoneId.of(
                    "America/Argentina/Cordoba"
            );


    private static final List<EstadoPedido> ESTADOS_EXCLUIDOS =
            List.of(
                    EstadoPedido.PENDIENTE_APROBACION,
                    EstadoPedido.CANCELADO
            );


    private final PedidoRepository pedidoRepository;


    @Override
    @Transactional(readOnly = true)
    public IngresosVentasResponse consultarIngresos(

            PeriodoIngreso periodo,

            LocalDate desde,

            LocalDate hasta
    ) {


        PeriodoIngreso periodoSeleccionado =
                resolverPeriodo(
                        periodo,
                        desde,
                        hasta
                );


        validarFechas(
                periodoSeleccionado,
                desde,
                hasta
        );


        LocalDate hoy =
                LocalDate.now(
                        ZONA_HORARIA
                );


        LocalDate fechaDesde =
                calcularFechaDesde(
                        periodoSeleccionado,
                        hoy,
                        desde
                );


        LocalDate fechaHasta =
                calcularFechaHasta(
                        periodoSeleccionado,
                        hoy,
                        fechaDesde,
                        hasta
                );


        LocalDateTime fechaHoraDesde =
                fechaDesde.atStartOfDay();


        LocalDateTime fechaHoraHastaExclusiva =
                fechaHasta
                        .plusDays(1)
                        .atStartOfDay();


        List<Pedido> ventasCobradas =
                pedidoRepository
                        .buscarIngresosPorPeriodo(
                                fechaHoraDesde,
                                fechaHoraHastaExclusiva,
                                EstadoPago.PAGADO,
                                ESTADOS_EXCLUIDOS
                        );


        long cantidadVentasCobradas =
                ventasCobradas.size();


        BigDecimal montoTotalIngresos =
                calcularMontoTotal(
                        ventasCobradas
                );


        return new IngresosVentasResponse(
                periodoSeleccionado,
                fechaDesde,
                fechaHasta,
                cantidadVentasCobradas,
                montoTotalIngresos
        );
    }


    private PeriodoIngreso resolverPeriodo(

            PeriodoIngreso periodo,

            LocalDate desde,

            LocalDate hasta
    ) {


        if (
                periodo != null
        ) {

            return periodo;
        }


        if (
                desde != null
                ||
                hasta != null
        ) {

            return PeriodoIngreso.RANGO;
        }


        return PeriodoIngreso.DIA;
    }


    private void validarFechas(

            PeriodoIngreso periodo,

            LocalDate desde,

            LocalDate hasta
    ) {


        if (
                periodo == PeriodoIngreso.RANGO
        ) {


            if (
                    desde == null
                    ||
                    hasta == null
            ) {

                throw new IllegalArgumentException(
                        "Para consultar un rango debe indicar fecha desde y fecha hasta"
                );
            }


            if (
                    desde.isAfter(
                            hasta
                    )
            ) {

                throw new IllegalArgumentException(
                        "La fecha desde no puede ser posterior a la fecha hasta"
                );
            }


            return;
        }


        if (
                desde != null
                ||
                hasta != null
        ) {

            throw new IllegalArgumentException(
                    "Las fechas desde y hasta solo pueden utilizarse con el período RANGO"
            );
        }
    }


    private LocalDate calcularFechaDesde(

            PeriodoIngreso periodo,

            LocalDate hoy,

            LocalDate desde
    ) {


        return switch (periodo) {


            case DIA ->
                    hoy;


            case SEMANA ->
                    hoy.with(
                            TemporalAdjusters.previousOrSame(
                                    DayOfWeek.MONDAY
                            )
                    );


            case MES ->
                    hoy.withDayOfMonth(
                            1
                    );


            case RANGO ->
                    desde;
        };
    }


    private LocalDate calcularFechaHasta(

            PeriodoIngreso periodo,

            LocalDate hoy,

            LocalDate fechaDesde,

            LocalDate hasta
    ) {


        return switch (periodo) {


            case DIA ->
                    hoy;


            case SEMANA ->
                    fechaDesde.plusDays(
                            6
                    );


            case MES ->
                    fechaDesde
                            .plusMonths(1)
                            .minusDays(1);


            case RANGO ->
                    hasta;
        };
    }


    private BigDecimal calcularMontoTotal(
            List<Pedido> pedidos
    ) {


        return pedidos
                .stream()

                .map(
                        pedido ->
                                pedido.getTotal() != null
                                        ? pedido.getTotal()
                                        : BigDecimal.ZERO
                )

                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}