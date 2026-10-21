package gestion_comercial.service.impl;

import gestion_comercial.dto.response.PedidoPendienteResponse;

import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.TipoVenta;

import gestion_comercial.exception.PedidoNoEncontradoException;

import gestion_comercial.repository.PedidoRepository;

import gestion_comercial.service.interfaces.IPedidoPendienteService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PedidoPendienteService
        implements IPedidoPendienteService {


    private final PedidoRepository
            pedidoRepository;


    @Override
    @Transactional(readOnly = true)
    public List<PedidoPendienteResponse>
    listarPendientes() {

        return pedidoRepository
                .findByTipoVentaAndEstadoPedidoAndClienteIsNotNullOrderByFechaCreacionDesc(
                        TipoVenta.ONLINE,
                        EstadoPedido.PENDIENTE_APROBACION
                )
                .stream()
                .map(
                        this::convertirResponse
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoPendienteResponse
    buscarPendiente(
            Integer idPedido
    ) {

        if (
                idPedido == null
                ||
                idPedido <= 0
        ) {

            throw new PedidoNoEncontradoException(
                    "El pedido pendiente indicado no existe"
            );
        }


        Pedido pedido =
                pedidoRepository
                        .findByIdPedidoAndTipoVentaAndEstadoPedidoAndClienteIsNotNull(
                                idPedido,
                                TipoVenta.ONLINE,
                                EstadoPedido.PENDIENTE_APROBACION
                        )
                        .orElseThrow(
                                () ->
                                        new PedidoNoEncontradoException(
                                                "El pedido pendiente indicado no existe"
                                        )
                        );


        return convertirResponse(
                pedido
        );
    }

    private PedidoPendienteResponse
    convertirResponse(
            Pedido pedido
    ) {

        Cliente cliente =
                pedido.getCliente();


        if (
                cliente == null
        ) {

            throw new PedidoNoEncontradoException(
                    "El pedido online no posee un cliente asociado"
            );
        }


        return new PedidoPendienteResponse(

                pedido.getIdPedido(),

                pedido.getNumeroPedido(),

                cliente.getIdCliente(),

                obtenerNombreCliente(
                        cliente
                ),

                pedido.getFechaCreacion(),

                pedido.getEstadoPedido(),

                pedido.getEstadoPago(),

                pedido.getMedioEntrega(),

                pedido.getLocalidadEnvio(),

                pedido.getDireccionEnvio(),

                pedido.getTotal()
        );
    }

    private String obtenerNombreCliente(
            Cliente cliente
    ) {

        if (
                cliente.getRazonSocial() != null
                &&
                !cliente.getRazonSocial()
                        .isBlank()
        ) {

            return cliente
                    .getRazonSocial()
                    .trim();
        }


        String nombre =
                cliente.getNombre() == null
                        ? ""
                        : cliente.getNombre().trim();


        String apellido =
                cliente.getApellido() == null
                        ? ""
                        : cliente.getApellido().trim();


        String nombreCompleto =
                (
                        nombre
                        +
                        " "
                        +
                        apellido
                ).trim();


        if (
                !nombreCompleto.isBlank()
        ) {

            return nombreCompleto;
        }


        return "Cliente #"
                + cliente.getIdCliente();
    }
}