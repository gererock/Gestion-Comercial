package gestion_comercial.service.impl;

import gestion_comercial.dto.request.ActualizarEstadoPedidoRequest;
import gestion_comercial.dto.response.PedidoEstadoResponse;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.MedioEntrega;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.TipoVenta;
import gestion_comercial.exception.EstadoPedidoInvalidoException;
import gestion_comercial.exception.PedidoNoEncontradoException;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.service.interfaces.IPedidoEstadoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PedidoEstadoService implements IPedidoEstadoService {

    private final PedidoRepository pedidoRepository;

    @Override
    @Transactional
    public PedidoEstadoResponse actualizar(
            Integer idPedido,
            ActualizarEstadoPedidoRequest request
    ) {
        Pedido pedido = obtenerPedido(idPedido);
        EstadoPedido nuevoEstado = convertirEstado(request.estadoPedido());

        EstadoPedido estadoPermitido = siguienteEstadoPermitido(pedido);

        if (estadoPermitido == null) {
            throw new EstadoPedidoInvalidoException(
                    mensajeSinTransicion(pedido.getEstadoPedido())
            );
        }

        if (nuevoEstado != estadoPermitido) {
            throw new EstadoPedidoInvalidoException(
                    "El pedido solo puede pasar de "
                            + nombreEstado(pedido.getEstadoPedido())
                            + " a " + nombreEstado(estadoPermitido)
            );
        }

        LocalDateTime ahora = LocalDateTime.now();
        pedido.setEstadoPedido(nuevoEstado);
        pedido.setFechaActualizacion(ahora);
        pedidoRepository.save(pedido);

        return new PedidoEstadoResponse(
                pedido.getIdPedido(),
                pedido.getNumeroPedido(),
                pedido.getEstadoPedido(),
                pedido.getFechaActualizacion(),
                "El estado del pedido fue actualizado correctamente"
        );
    }

    private Pedido obtenerPedido(Integer idPedido) {
        if (idPedido == null || idPedido <= 0) {
            throw new PedidoNoEncontradoException(
                    "El pedido indicado no existe"
            );
        }

        Pedido pedido = pedidoRepository
                .buscarPorIdParaDecision(idPedido)
                .orElseThrow(() -> new PedidoNoEncontradoException(
                        "El pedido indicado no existe"
                ));

        if (pedido.getTipoVenta() != TipoVenta.ONLINE) {
            throw new EstadoPedidoInvalidoException(
                    "Solo se puede actualizar el estado de pedidos online"
            );
        }

        return pedido;
    }

    private EstadoPedido siguienteEstadoPermitido(Pedido pedido) {
        EstadoPedido estadoActual = pedido.getEstadoPedido();

        if (estadoActual == null) {
            return null;
        }

        return switch (estadoActual) {
            case CONFIRMADO -> EstadoPedido.EN_PREPARACION;

            case EN_PREPARACION -> switch (pedido.getMedioEntrega()) {
                case RETIRO_LOCAL -> EstadoPedido.LISTO_PARA_RETIRAR;
                case ENVIO -> EstadoPedido.LISTO_PARA_ENVIAR;
                case null -> null;
            };

            case LISTO_PARA_RETIRAR ->
                    pedido.getMedioEntrega() == MedioEntrega.RETIRO_LOCAL
                            ? EstadoPedido.ENTREGADO
                            : null;

            case LISTO_PARA_ENVIAR ->
                    pedido.getMedioEntrega() == MedioEntrega.ENVIO
                            ? EstadoPedido.ENVIADO
                            : null;

            case ENVIADO ->
                    pedido.getMedioEntrega() == MedioEntrega.ENVIO
                            ? EstadoPedido.ENTREGADO
                            : null;

            case PENDIENTE_APROBACION, ENTREGADO, CANCELADO -> null;
        };
    }

    private EstadoPedido convertirEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new EstadoPedidoInvalidoException(
                    "El nuevo estado es obligatorio"
            );
        }

        try {
            return EstadoPedido.valueOf(estado);
        } catch (IllegalArgumentException exception) {
            throw new EstadoPedidoInvalidoException(
                    "El estado indicado no es válido"
            );
        }
    }

    private String mensajeSinTransicion(EstadoPedido estadoActual) {
        if (estadoActual == EstadoPedido.PENDIENTE_APROBACION) {
            return "El pedido debe ser aprobado antes de iniciar la preparación";
        }
        if (estadoActual == EstadoPedido.ENTREGADO) {
            return "No se puede modificar un pedido entregado";
        }
        if (estadoActual == EstadoPedido.CANCELADO) {
            return "No se puede modificar un pedido cancelado";
        }

        return "El pedido no tiene una transición de estado válida para su modalidad de entrega";
    }

    private String nombreEstado(EstadoPedido estado) {
        return estado.name()
                .toLowerCase()
                .replace('_', ' ');
    }
}
