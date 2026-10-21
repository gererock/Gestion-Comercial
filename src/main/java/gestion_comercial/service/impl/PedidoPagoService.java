package gestion_comercial.service.impl;

import gestion_comercial.dto.request.EstadoPagoPedidoRequest;
import gestion_comercial.dto.response.EstadoPagoPedidoResponse;

import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.TipoVenta;

import gestion_comercial.exception.EstadoPagoPedidoInvalidoException;
import gestion_comercial.exception.PedidoNoEncontradoException;

import gestion_comercial.repository.PedidoRepository;

import gestion_comercial.service.interfaces.IPedidoPagoService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class PedidoPagoService
        implements IPedidoPagoService {

    private final PedidoRepository
            pedidoRepository;


    public PedidoPagoService(
            PedidoRepository pedidoRepository
    ) {

        this.pedidoRepository =
                pedidoRepository;
    }


    // =========================================
    // CONSULTAR ESTADO DE PAGO - US20
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public EstadoPagoPedidoResponse consultar(
            Integer idPedido
    ) {

        Pedido pedido =
                obtenerPedidoOnline(
                        idPedido
                );


        return convertirResponse(
                pedido
        );
    }


    // =========================================
    // ACTUALIZAR ESTADO DE PAGO - US20
    // =========================================

    @Override
    @Transactional
    public EstadoPagoPedidoResponse actualizar(

            Integer idPedido,

            EstadoPagoPedidoRequest request
    ) {

        Pedido pedido =
                obtenerPedidoOnline(
                        idPedido
                );


        EstadoPago nuevoEstado =
                request.estadoPago();


        if (
                nuevoEstado == null
        ) {

            throw new EstadoPagoPedidoInvalidoException(
                    "El estado de pago es obligatorio"
            );
        }


        if (
                pedido.getMedioPago()
                == null
        ) {

            throw new EstadoPagoPedidoInvalidoException(
                    "El pedido no tiene un medio de pago asociado"
            );
        }


        if (
                pedido.getEstadoPedido()
                ==
                EstadoPedido.CANCELADO
                &&
                nuevoEstado
                ==
                EstadoPago.PAGADO
        ) {

            throw new EstadoPagoPedidoInvalidoException(
                    "No se puede marcar como pagado un pedido cancelado"
            );
        }


        String descripcion =
                normalizarDescripcion(
                        request.descripcionPago()
                );


        pedido.setEstadoPago(
                nuevoEstado
        );


        /*
         * Una descripción de pago pendiente tiene sentido
         * únicamente mientras el pago permanezca pendiente.
         *
         * Al marcar como PAGADO eliminamos la descripción
         * anterior para no dejar información contradictoria.
         */

        pedido.setDescripcionPago(

                nuevoEstado
                        ==
                        EstadoPago.PENDIENTE

                        ? descripcion

                        : null
        );


        pedido.setFechaActualizacion(
                LocalDateTime.now()
        );


        Pedido guardado =
                pedidoRepository.save(
                        pedido
                );


        return convertirResponse(
                guardado
        );
    }


    // =========================================
    // OBTENER PEDIDO ONLINE
    // =========================================

    private Pedido obtenerPedidoOnline(
            Integer idPedido
    ) {

        if (
                idPedido == null
                ||
                idPedido <= 0
        ) {

            throw new PedidoNoEncontradoException(
                    "El pedido indicado no existe"
            );
        }


        Pedido pedido =
                pedidoRepository
                        .findById(
                                idPedido
                        )
                        .orElseThrow(
                                () ->
                                        new PedidoNoEncontradoException(
                                                "El pedido indicado no existe"
                                        )
                        );


        /*
         * Las ventas locales gestionarán su pago
         * mediante US26.
         */

        if (
                pedido.getTipoVenta()
                !=
                TipoVenta.ONLINE
        ) {

            throw new EstadoPagoPedidoInvalidoException(
                    "Esta funcionalidad solo permite gestionar pagos de pedidos online"
            );
        }


        return pedido;
    }


    // =========================================
    // DESCRIPCIÓN
    // =========================================

    private String normalizarDescripcion(
            String descripcion
    ) {

        if (
                descripcion == null
        ) {

            return null;
        }


        String normalizada =
                descripcion
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        /*
         * La descripción es opcional.
         */

        if (
                normalizada.isEmpty()
        ) {

            return null;
        }


        /*
         * Si se escribe una descripción,
         * debe contener información real.
         *
         * Evita entradas como:
         * @@@@
         * ####
         */

        if (
                !normalizada.matches(
                        ".*[\\p{L}\\p{N}].*"
                )
        ) {

            throw new EstadoPagoPedidoInvalidoException(
                    "La descripción del pago no es válida"
            );
        }


        return normalizada;
    }


    // =========================================
    // RESPONSE
    // =========================================

    private EstadoPagoPedidoResponse
    convertirResponse(
            Pedido pedido
    ) {

        return new EstadoPagoPedidoResponse(

                pedido.getIdPedido(),

                pedido.getNumeroPedido(),

                pedido.getEstadoPedido(),

                pedido.getEstadoPago(),

                pedido.getDescripcionPago(),

                pedido.getMedioPago()
                        .getIdMedioPago(),

                pedido.getMedioPago()
                        .getNombre(),

                pedido.getFechaActualizacion()
        );
    }
}