package gestion_comercial.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DescuentoClienteRequest(

        @Size(
                max = 6,
                message = "El descuento ingresado es demasiado largo"
        )
        @Pattern(
                regexp = "^$|^[0-9]+(?:\\.[0-9]{1,2})?$",
                message = "El descuento debe ser numérico y puede tener como máximo dos decimales"
        )
        String descuentoPorcentaje
) {
}
