package gestion_comercial.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GrupoCatalogoCreateRequest(

        @NotBlank(
                message = "El nombre visible es obligatorio"
        )
        @Size(
                max = 120,
                message = "El nombre visible no puede superar los 120 caracteres"
        )
        @Pattern(
                regexp = "^(?=.*\\p{L})[\\p{L}\\p{N}\\s°ºª.\\-]+$",
                message = "El nombre visible debe contener al menos una letra y no puede contener símbolos inválidos"
        )
        String nombreVisible,

        @Size(
                max = 500,
                message = "La descripción no puede superar los 500 caracteres"
        )
        String descripcion,

        @Size(
                max = 255,
                message = "La URL de la imagen no puede superar los 255 caracteres"
        )
        String imagenUrl

) {
}