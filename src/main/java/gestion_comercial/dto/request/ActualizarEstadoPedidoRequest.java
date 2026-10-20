package gestion_comercial.dto.request;

import gestion_comercial.entity.EstadoPedido;

import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoPedidoRequest(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoPedido estadoPedido
) {
}
