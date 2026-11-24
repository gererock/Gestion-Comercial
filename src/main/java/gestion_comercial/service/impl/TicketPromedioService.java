package gestion_comercial.service.impl;

import gestion_comercial.dto.response.TicketPromedioResponse;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.PeriodoTicketPromedio;
import gestion_comercial.entity.TipoTicketPromedio;
import gestion_comercial.entity.TipoVenta;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.service.interfaces.ITicketPromedioService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TicketPromedioService
        implements ITicketPromedioService {


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
    public TicketPromedioResponse consultarTicketPromedio(

            PeriodoTicketPromedio periodo,

            TipoTicketPromedio tipo,

            LocalDate desde,

            LocalDate hasta
    ) {


        PeriodoTicketPromedio periodoSeleccionado =
                resolverPeriodo(
                        periodo,
                        desde,
                        hasta
                );


        TipoTicketPromedio tipoSeleccionado =
                resolverTipo(
                        tipo
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


        List<Pedido> ventasFiltradas =
                ventasValidas
                        .stream()
                        .filter(
                                pedido ->
                                        correspondeAlTipo(
                                                pedido,
                                                tipoSeleccionado
                                        )
                        )
                        .toList();


        long cantidadVentas =
                ventasFiltradas.size();


        BigDecimal totalVendido =
                calcularTotalVendido(
                        ventasFiltradas
                );


        BigDecimal ticketPromedio =
                calcularTicketPromedio(
                        totalVendido,
                        cantidadVentas
                );


        return new TicketPromedioResponse(
                periodoSeleccionado,
                fechaDesde,
                fechaHasta,
                tipoSeleccionado,
                cantidadVentas,
                totalVendido,
                ticketPromedio
        );
    }


    private PeriodoTicketPromedio resolverPeriodo(

            PeriodoTicketPromedio periodo,

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

            return PeriodoTicketPromedio.RANGO;
        }


        return PeriodoTicketPromedio.DIA;
    }


    private TipoTicketPromedio resolverTipo(
            TipoTicketPromedio tipo
    ) {


        if (
                tipo != null
        ) {

            return tipo;
        }


        return TipoTicketPromedio.GENERAL;
    }


    private void validarFechas(

            PeriodoTicketPromedio periodo,

            LocalDate desde,

            LocalDate hasta
    ) {


        if (
                periodo == PeriodoTicketPromedio.RANGO
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

            PeriodoTicketPromedio periodo,

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

            PeriodoTicketPromedio periodo,

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


    private boolean correspondeAlTipo(

            Pedido pedido,

            TipoTicketPromedio tipo
    ) {


        return switch (tipo) {


            case GENERAL ->
                    pedido.getTipoVenta() == TipoVenta.ONLINE
                    ||
                    pedido.getTipoVenta() == TipoVenta.LOCAL;


            case ONLINE ->
                    pedido.getTipoVenta() == TipoVenta.ONLINE;


            case LOCAL ->
                    pedido.getTipoVenta() == TipoVenta.LOCAL;
        };
    }


    private BigDecimal calcularTotalVendido(
            List<Pedido> ventas
    ) {


        return ventas
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


    private BigDecimal calcularTicketPromedio(

            BigDecimal totalVendido,

            long cantidadVentas
    ) {


        if (
                cantidadVentas == 0
        ) {

            return BigDecimal.ZERO;
        }


        return totalVendido.divide(
                BigDecimal.valueOf(
                        cantidadVentas
                ),
                2,
                RoundingMode.HALF_UP
        );
    }
}