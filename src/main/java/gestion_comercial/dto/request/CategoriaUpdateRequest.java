package gestion_comercial.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoriaUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        @Pattern(
                regexp = "^(?=.*\\p{L})[\\p{L}\\p{N}\\s°ºª.\\-]+$",
                message = "El nombre debe contener al menos una letra y no puede contener símbolos inválidos")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion
) {


}
