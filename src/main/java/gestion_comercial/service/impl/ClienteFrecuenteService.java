package gestion_comercial.service.impl;

import gestion_comercial.dto.response.ClienteFrecuenteResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.OrdenClienteFrecuente;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.PeriodoClienteFrecuente;
import gestion_comercial.entity.TipoVenta;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.service.interfaces.IClienteFrecuenteService;

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
import java.util.Locale;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ClienteFrecuenteService
        implements IClienteFrecuenteService {


    private static final ZoneId ZONA_HORARIA =
            ZoneId.of(
                    "America/Argentina/Cordoba"
            );


    private static final List<EstadoPedido> ESTADOS_EXCLUIDOS =
            List.of(
                    EstadoPedido.PENDIENTE_APROBACION,
                    EstadoPedido.CANCELADO
            );


    private final PedidoRepository
            pedidoRepository;


    @Override
    @Transactional(readOnly = true)
    public List<ClienteFrecuenteResponse> consultarClientesFrecuentes(

            PeriodoClienteFrecuente periodo,

            LocalDate desde,

            LocalDate hasta,

            OrdenClienteFrecuente orden,

            String buscar

    ) {

        PeriodoClienteFrecuente periodoSeleccionado =
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


        OrdenClienteFrecuente ordenSeleccionado =
                resolverOrden(
                        orden
                );


        String busquedaNormalizada =
                normalizarBusqueda(
                        buscar
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


        List<Pedido> pedidos =
                pedidoRepository
                        .buscarVentasValidasPorPeriodo(
                                fechaHoraDesde,
                                fechaHoraHastaExclusiva,
                                ESTADOS_EXCLUIDOS
                        );


        Map<Integer, ClienteAcumulado> clientesAcumulados =
                new HashMap<>();


        for (
                Pedido pedido
                :
                pedidos
        ) {

            if (
                    pedido.getTipoVenta()
                    !=
                    TipoVenta.ONLINE

                    &&

                    pedido.getTipoVenta()
                    !=
                    TipoVenta.LOCAL
            ) {

                continue;
            }


            Cliente cliente =
                    pedido.getCliente();


            if (
                    cliente == null
            ) {

                continue;
            }


            Integer idCliente =
                    cliente.getIdCliente();


            ClienteAcumulado acumulado =
                    clientesAcumulados
                            .computeIfAbsent(
                                    idCliente,
                                    id ->
                                            new ClienteAcumulado(
                                                    cliente
                                            )
                            );


            acumulado.agregar(
                    pedido
            );
        }


        return clientesAcumulados
                .values()
                .stream()
                .map(
                        this::convertirAResponse
                )
                .filter(
                        cliente ->
                                coincideBusqueda(
                                        cliente,
                                        busquedaNormalizada
                                )
                )
                .sorted(
                        obtenerComparador(
                                ordenSeleccionado
                        )
                )
                .toList();
    }


    private PeriodoClienteFrecuente resolverPeriodo(

            PeriodoClienteFrecuente periodo,

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

            return PeriodoClienteFrecuente.RANGO;
        }


        return PeriodoClienteFrecuente.DIA;
    }


    private OrdenClienteFrecuente resolverOrden(

            OrdenClienteFrecuente orden

    ) {

        if (
                orden != null
        ) {

            return orden;
        }


        return OrdenClienteFrecuente.CANTIDAD_COMPRAS;
    }


    private void validarFechas(

            PeriodoClienteFrecuente periodo,

            LocalDate desde,

            LocalDate hasta

    ) {

        if (
                periodo
                ==
                PeriodoClienteFrecuente.RANGO
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


    private String normalizarBusqueda(

            String buscar

    ) {

        if (
                buscar == null
        ) {

            return null;
        }


        if (
                buscar.isEmpty()
        ) {

            return null;
        }


        if (
                buscar.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La búsqueda no puede contener solamente espacios"
            );
        }


        String busqueda =
                buscar.trim();


        boolean tieneCaracterUtil =
                busqueda
                        .chars()
                        .anyMatch(
                                Character::isLetterOrDigit
                        );


        if (
                !tieneCaracterUtil
        ) {

            throw new IllegalArgumentException(
                    "La búsqueda debe contener letras o números"
            );
        }


        return busqueda
                .toLowerCase(
                        Locale.ROOT
                );
    }


    private LocalDate calcularFechaDesde(

            PeriodoClienteFrecuente periodo,

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

            PeriodoClienteFrecuente periodo,

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


    private ClienteFrecuenteResponse convertirAResponse(

            ClienteAcumulado acumulado

    ) {

        Cliente cliente =
                acumulado.cliente;


        return new ClienteFrecuenteResponse(

                cliente.getIdCliente(),

                cliente.getNombre(),

                cliente.getApellido(),

                cliente.getRazonSocial(),

                cliente.getTelefono(),

                cliente.getCorreo(),

                acumulado.cantidadCompras,

                acumulado.totalComprado
        );
    }


    private boolean coincideBusqueda(

            ClienteFrecuenteResponse cliente,

            String buscar

    ) {

        if (
                buscar == null
        ) {

            return true;
        }


        return contiene(
                cliente.nombre(),
                buscar
        )
                ||
                contiene(
                        cliente.apellido(),
                        buscar
                )
                ||
                contiene(
                        cliente.razonSocial(),
                        buscar
                );
    }


    private boolean contiene(

            String valor,

            String buscar

    ) {

        if (
                valor == null
        ) {

            return false;
        }


        return valor
                .toLowerCase(
                        Locale.ROOT
                )
                .contains(
                        buscar
                );
    }


    private Comparator<ClienteFrecuenteResponse> obtenerComparador(

            OrdenClienteFrecuente orden

    ) {

        return switch (
                orden
        ) {

            case CANTIDAD_COMPRAS ->

                    Comparator
                            .comparingLong(
                                    ClienteFrecuenteResponse::cantidadCompras
                            )
                            .reversed()
                            .thenComparing(
                                    ClienteFrecuenteResponse::totalComprado,
                                    Comparator.reverseOrder()
                            )
                            .thenComparing(
                                    ClienteFrecuenteResponse::idCliente
                            );


            case TOTAL_COMPRADO ->

                    Comparator
                            .comparing(
                                    ClienteFrecuenteResponse::totalComprado,
                                    Comparator.reverseOrder()
                            )
                            .thenComparing(
                                    Comparator
                                            .comparingLong(
                                                    ClienteFrecuenteResponse::cantidadCompras
                                            )
                                            .reversed()
                            )
                            .thenComparing(
                                    ClienteFrecuenteResponse::idCliente
                            );
        };
    }


    private static class ClienteAcumulado {


        private final Cliente cliente;


        private long cantidadCompras;


        private BigDecimal totalComprado;


        private ClienteAcumulado(

                Cliente cliente

        ) {

            this.cliente =
                    cliente;


            this.cantidadCompras =
                    0;


            this.totalComprado =
                    BigDecimal.ZERO;
        }


        private void agregar(

                Pedido pedido

        ) {

            cantidadCompras++;


            if (
                    pedido.getTotal()
                    != null
            ) {

                totalComprado =
                        totalComprado.add(
                                pedido.getTotal()
                        );
            }
        }
    }
}