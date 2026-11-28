package gestion_comercial.service.impl;

import gestion_comercial.dto.response.PedidoPendienteResponse;

import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.MedioEntrega;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.TipoVenta;

import gestion_comercial.exception.PedidoNoEncontradoException;

import gestion_comercial.repository.PedidoRepository;

import gestion_comercial.service.interfaces.IPedidoPendienteService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.util.List;
import java.util.ArrayList;


@Service
@RequiredArgsConstructor
public class PedidoPendienteService
        implements IPedidoPendienteService {


    private final PedidoRepository
            pedidoRepository;


    @Override
    @Transactional(readOnly = true)
    public List<PedidoPendienteResponse> listarPendientes() {
        return listarPendientes(null, null, null, null);
    }


    @Override
    @Transactional(readOnly = true)
    public List<PedidoPendienteResponse>
    listarPendientes(
            String busqueda,
            EstadoPedido estadoPedido,
            MedioEntrega medioEntrega,
            EstadoPago estadoPago
    ) {
        Specification<Pedido> specification = (root, query, criteria) -> {
            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(criteria.equal(root.get("tipoVenta"), TipoVenta.ONLINE));
            condiciones.add(root.get("estadoPedido").in(
                    EstadoPedido.PENDIENTE_APROBACION,
                    EstadoPedido.CONFIRMADO,
                    EstadoPedido.EN_PREPARACION,
                    EstadoPedido.LISTO_PARA_RETIRAR,
                    EstadoPedido.LISTO_PARA_ENVIAR,
                    EstadoPedido.ENVIADO
            ));
            condiciones.add(criteria.isNotNull(root.get("cliente")));

            if (estadoPedido != null) {
                condiciones.add(criteria.equal(root.get("estadoPedido"), estadoPedido));
            }
            if (medioEntrega != null) {
                condiciones.add(criteria.equal(root.get("medioEntrega"), medioEntrega));
            }
            if (estadoPago != null) {
                condiciones.add(criteria.equal(root.get("estadoPago"), estadoPago));
            }

            String termino = busqueda == null ? "" : busqueda.trim().toLowerCase();
            if (!termino.isEmpty()) {
                String patron = "%" + termino + "%";
                var cliente = root.join("cliente");
                var nombreCompleto = criteria.lower(criteria.concat(
                        criteria.concat(
                                criteria.<String>coalesce(cliente.<String>get("nombre"), ""),
                                " "
                        ),
                        criteria.<String>coalesce(cliente.<String>get("apellido"), "")
                ));
                condiciones.add(criteria.or(
                        criteria.like(criteria.lower(root.<String>get("numeroPedido")), patron),
                        criteria.like(nombreCompleto, patron),
                        criteria.like(criteria.lower(criteria.<String>coalesce(
                                cliente.<String>get("razonSocial"), ""
                        )), patron)
                ));
            }

            return criteria.and(condiciones.toArray(Predicate[]::new));
        };

        return pedidoRepository.findAll(
                        specification,
                        Sort.by(Sort.Order.desc("fechaCreacion"), Sort.Order.desc("idPedido"))
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
