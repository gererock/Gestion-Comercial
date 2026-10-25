package gestion_comercial.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReposicionStockRequest(

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

        @NotBlank(message = "La cantidad a reponer es obligatoria")
        @Pattern(
                regexp = "^[1-9][0-9]*$",
                message = "La cantidad debe ser un número entero mayor a cero"
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
