package gestion_comercial.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemConfirmarPedidoRequest(

        @NotNull(message = "El producto es obligatorio")
        @Positive(message = "El producto debe ser válido")
        Integer idProducto,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        Integer cantidad

) {
}