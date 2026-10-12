package gestion_comercial.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public record ImagenProductoOrdenRequest(

        @NotNull(
                message = "El orden es obligatorio"
        )
        @Min(
                value = 0,
                message = "El orden no puede ser negativo"
        )
        Integer orden

) {
}