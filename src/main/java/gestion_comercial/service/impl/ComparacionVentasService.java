package gestion_comercial.service.impl;

import gestion_comercial.dto.response.ComparacionVentasResponse;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.PeriodoComparacionVenta;
import gestion_comercial.entity.TipoVenta;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.service.interfaces.IComparacionVentasService;

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
public class ComparacionVentasService
        implements IComparacionVentasService {


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
    public ComparacionVentasResponse compararVentas(

            PeriodoComparacionVenta periodo,

            LocalDate desde,

            LocalDate hasta
    ) {


        PeriodoComparacionVenta periodoSeleccionado =
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


        List<Pedido> ventasValidas =
                pedidoRepository
                        .buscarVentasValidasPorPeriodo(
                                fechaHoraDesde,
                                fechaHoraHastaExclusiva,
                                ESTADOS_EXCLUIDOS
                        );


        long cantidadVentasOnline =
                0;


        BigDecimal montoTotalOnline =
                BigDecimal.ZERO;


        long cantidadVentasLocales =
                0;


        BigDecimal montoTotalLocal =
                BigDecimal.ZERO;


        for (
                Pedido pedido
                :
                ventasValidas
        ) {


            BigDecimal totalPedido =
                    obtenerTotalPedido(
                            pedido
                    );


            if (
                    pedido.getTipoVenta()
                            == TipoVenta.ONLINE
            ) {


                cantidadVentasOnline++;


                montoTotalOnline =
                        montoTotalOnline.add(
                                totalPedido
                        );


            } else if (
                    pedido.getTipoVenta()
                            == TipoVenta.LOCAL
            ) {


                cantidadVentasLocales++;


                montoTotalLocal =
                        montoTotalLocal.add(
                                totalPedido
                        );
            }
        }


        return new ComparacionVentasResponse(
                periodoSeleccionado,
                fechaDesde,
                fechaHasta,
                cantidadVentasOnline,
                montoTotalOnline,
                cantidadVentasLocales,
                montoTotalLocal
        );
    }


    private PeriodoComparacionVenta resolverPeriodo(

            PeriodoComparacionVenta periodo,

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

            return PeriodoComparacionVenta.RANGO;
        }


        return PeriodoComparacionVenta.DIA;
    }


    private void validarFechas(

            PeriodoComparacionVenta periodo,

            LocalDate desde,

            LocalDate hasta
    ) {


        if (
                periodo == PeriodoComparacionVenta.RANGO
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

            PeriodoComparacionVenta periodo,

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

            PeriodoComparacionVenta periodo,

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


    private BigDecimal obtenerTotalPedido(
            Pedido pedido
    ) {


        if (
                pedido.getTotal() == null
        ) {

            return BigDecimal.ZERO;
        }


        return pedido.getTotal();
    }
}