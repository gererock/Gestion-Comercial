package gestion_comercial.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MarcaCreateRequest(
    @NotNull(message = "LA marca debe tener nombre")
    @Pattern(
                regexp = "^(?=.*\\p{L})[\\p{L}\\p{N}\\s°ºª.\\-]+$",
                message = "El nombre debe contener al menos una letra y no puede contener símbolos inválidos")
    String nombre
) {

}
