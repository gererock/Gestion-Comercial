package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoPedido;

import java.time.LocalDateTime;

public record PedidoDecisionResponse(
        Integer idPedido,
        String numeroPedido,
        EstadoPedido estadoPedido,
        LocalDateTime fechaActualizacion,
        String mensaje
) {
}
