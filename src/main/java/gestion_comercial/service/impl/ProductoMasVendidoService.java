package gestion_comercial.service.impl;

import gestion_comercial.dto.response.ProductoMasVendidoResponse;
import gestion_comercial.entity.DetallePedido;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.PeriodoProductoVendido;
import gestion_comercial.entity.Producto;
import gestion_comercial.repository.DetallePedidoRepository;
import gestion_comercial.service.interfaces.IProductoMasVendidoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ProductoMasVendidoService
        implements IProductoMasVendidoService {


    private static final ZoneId ZONA_HORARIA =
            ZoneId.of(
                    "America/Argentina/Cordoba"
            );


    private static final List<EstadoPedido> ESTADOS_EXCLUIDOS =
            List.of(
                    EstadoPedido.PENDIENTE_APROBACION,
                    EstadoPedido.CANCELADO
            );


    private final DetallePedidoRepository
            detallePedidoRepository;


    @Override
    @Transactional(readOnly = true)
    public List<ProductoMasVendidoResponse> consultarProductosMasVendidos(

            PeriodoProductoVendido periodo,

            LocalDate desde,

            LocalDate hasta

    ) {

        PeriodoProductoVendido periodoSeleccionado =
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


        List<DetallePedido> detalles =
                detallePedidoRepository
                        .buscarDetallesVentasValidasPorPeriodo(
                                fechaHoraDesde,
                                fechaHoraHastaExclusiva,
                                ESTADOS_EXCLUIDOS
                        );


        Map<Integer, ProductoAcumulado> productosAcumulados =
                new HashMap<>();


        for (
                DetallePedido detalle
                :
                detalles
        ) {

            Producto producto =
                    detalle.getProducto();


            Integer idProducto =
                    producto.getIdProducto();


            ProductoAcumulado acumulado =
                    productosAcumulados
                            .computeIfAbsent(
                                    idProducto,
                                    id ->
                                            new ProductoAcumulado(
                                                    producto
                                            )
                            );


            acumulado.agregar(
                    detalle
            );
        }


        return productosAcumulados
                .values()
                .stream()
                .map(
                        this::convertirAResponse
                )
                .sorted(
                        Comparator
                                .comparingLong(
                                        ProductoMasVendidoResponse::cantidadVendida
                                )
                                .reversed()
                                .thenComparing(
                                        ProductoMasVendidoResponse::idProducto
                                )
                )
                .toList();
    }


    private PeriodoProductoVendido resolverPeriodo(

            PeriodoProductoVendido periodo,

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

            return PeriodoProductoVendido.RANGO;
        }


        return PeriodoProductoVendido.DIA;
    }


    private void validarFechas(

            PeriodoProductoVendido periodo,

            LocalDate desde,

            LocalDate hasta

    ) {

        if (
                periodo
                ==
                PeriodoProductoVendido.RANGO
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

            PeriodoProductoVendido periodo,

            LocalDate hoy,

            LocalDate desde

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


            case RANGO ->
                    desde;
        };
    }


    private LocalDate calcularFechaHasta(

            PeriodoProductoVendido periodo,

            LocalDate hoy,

            LocalDate fechaDesde,

            LocalDate hasta

    ) {

        return switch (
                periodo
        ) {

            case DIA ->
                    hoy;


            case SEMANA ->
                    fechaDesde.plusDays(
                            6
                    );


            case MES ->
                    fechaDesde
                            .plusMonths(
                                    1
                            )
                            .minusDays(
                                    1
                            );


            case RANGO ->
                    hasta;
        };
    }


    private ProductoMasVendidoResponse convertirAResponse(

            ProductoAcumulado acumulado

    ) {

        Producto producto =
                acumulado.producto;


        String categoria =
                producto.getCategoria()
                        != null

                        ? producto
                                .getCategoria()
                                .getNombre()

                        : null;


        String marca =
                producto.getMarca()
                        != null

                        ? producto
                                .getMarca()
                                .getNombre()

                        : null;


        return new ProductoMasVendidoResponse(

                producto.getIdProducto(),

                producto.getNombre(),

                categoria,

                marca,

                acumulado.cantidadVendida,

                acumulado.totalVendido
        );
    }


    private static class ProductoAcumulado {


        private final Producto producto;


        private long cantidadVendida;


        private BigDecimal totalVendido;


        private ProductoAcumulado(
                Producto producto
        ) {

            this.producto =
                    producto;


            this.cantidadVendida =
                    0;


            this.totalVendido =
                    BigDecimal.ZERO;
        }


        private void agregar(
                DetallePedido detalle
        ) {

            if (
                    detalle.getCantidad()
                    != null
            ) {

                cantidadVendida +=
                        detalle.getCantidad();
            }


            if (
                    detalle.getSubtotal()
                    != null
            ) {

                totalVendido =
                        totalVendido.add(
                                detalle.getSubtotal()
                        );
            }
        }
    }
}