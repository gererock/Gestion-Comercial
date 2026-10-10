package gestion_comercial.dto.request;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MarcaCreateRequest(
    @NotBlank (message = "LA marca debe tener nombre")
    @Size(max = 100, message = "La marca no puede superar los 100 caracteres")
    @Pattern(
                regexp = "^(?=.*\\p{L})[\\p{L}\\p{N}\\s°ºª.\\-]+$",
                message = "El nombre debe contener al menos una letra y no puede contener símbolos inválidos")
    String nombre
) {

}
