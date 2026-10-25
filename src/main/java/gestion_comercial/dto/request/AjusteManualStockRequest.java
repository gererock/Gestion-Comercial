package gestion_comercial.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AjusteManualStockRequest(

        @NotBlank(message = "Debe seleccionar un producto")
        @Pattern(
                regexp = "^[1-9][0-9]*$",
                message = "El producto seleccionado no es válido"
        )
        @Size(
                max = 10,
                message = "El producto seleccionado no es válido"
        )
        String idProducto,

        @NotBlank(message = "Debe seleccionar un tipo de ajuste")
        @Pattern(
                regexp = "^(POSITIVO|NEGATIVO)$",
                message = "El tipo de ajuste seleccionado no es válido"
        )
        String tipoAjuste,

        @NotBlank(message = "La cantidad del ajuste es obligatoria")
        @Pattern(
                regexp = "^[1-9][0-9]*$",
                message = "La cantidad debe ser un número entero mayor a cero"
        )
        @Pattern(
                regexp = "^(?:[1-9][0-9]{0,5}|1000000)$",
                message = "La cantidad máxima permitida por ajuste es 1000000"
        )
        @Size(
                max = 7,
                message = "La cantidad ingresada supera el límite permitido"
        )
        String cantidad,

        @NotBlank(message = "No se pudo identificar la operación")
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
                message = "La identificación de la operación no es válida"
        )
        String claveOperacion
) {
}
