package gestion_comercial.service.impl;

import gestion_comercial.dto.response.VentasPeriodoResponse;
import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.PeriodoVenta;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.service.interfaces.IVentasPeriodoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;


@Service
@RequiredArgsConstructor
public class VentasPeriodoService
        implements IVentasPeriodoService {


    private final PedidoRepository pedidoRepository;


    private static final List<EstadoPedido>
            ESTADOS_EXCLUIDOS = List.of(

                    EstadoPedido.PENDIENTE_APROBACION,

                    EstadoPedido.CANCELADO
            );


    @Override
    @Transactional(readOnly = true)
    public VentasPeriodoResponse consultarVentasPorPeriodo(
            PeriodoVenta periodo
    ) {

        if (
                periodo == null
        ) {

            throw new IllegalArgumentException(
                    "El período es obligatorio"
            );
        }


        LocalDate hoy =
                LocalDate.now();


        LocalDate fechaDesde =
                calcularFechaDesde(
                        periodo,
                        hoy
                );


        LocalDate fechaHastaExclusiva =
                calcularFechaHastaExclusiva(
                        periodo,
                        fechaDesde
                );


        LocalDateTime desde =
                fechaDesde.atStartOfDay();


        LocalDateTime hasta =
                fechaHastaExclusiva.atStartOfDay();


        List<Pedido> ventasRealizadas =
                pedidoRepository
                        .buscarVentasValidasPorPeriodo(

                                desde,

                                hasta,

                                ESTADOS_EXCLUIDOS
                        );


        long cantidadVentasRealizadas =
                ventasRealizadas.size();


        BigDecimal montoTotalVendido =
                calcularMontoTotal(
                        ventasRealizadas
                );


        List<Pedido> ventasCobradas =
                ventasRealizadas
                        .stream()
                        .filter(
                                pedido ->
                                        pedido.getEstadoPago()
                                                == EstadoPago.PAGADO
                        )
                        .toList();


        long cantidadVentasCobradas =
                ventasCobradas.size();


        BigDecimal montoTotalCobrado =
                calcularMontoTotal(
                        ventasCobradas
                );


        LocalDate fechaHasta =
                fechaHastaExclusiva
                        .minusDays(
                                1
                        );


        return new VentasPeriodoResponse(

                periodo,

                fechaDesde,

                fechaHasta,

                cantidadVentasRealizadas,

                montoTotalVendido,

                cantidadVentasCobradas,

                montoTotalCobrado
        );
    }


    private LocalDate calcularFechaDesde(

            PeriodoVenta periodo,

            LocalDate hoy
    ) {

        return switch (
        periodo
        ) {

            case DIA ->
                    hoy;


            case SEMANA ->
                    hoy.with(

                            TemporalAdjusters
                                    .previousOrSame(
                                            DayOfWeek.MONDAY
                                    )
                    );


            case MES ->
                    hoy.withDayOfMonth(
                            1
                    );
        };
    }


    private LocalDate calcularFechaHastaExclusiva(

            PeriodoVenta periodo,

            LocalDate fechaDesde
    ) {

        return switch (
        periodo
        ) {

            case DIA ->
                    fechaDesde.plusDays(
                            1
                    );


            case SEMANA ->
                    fechaDesde.plusWeeks(
                            1
                    );


            case MES ->
                    fechaDesde.plusMonths(
                            1
                    );
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