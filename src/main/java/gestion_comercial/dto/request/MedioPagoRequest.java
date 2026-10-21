package gestion_comercial.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record MedioPagoRequest(

        @NotBlank(
                message = "El nombre del medio de pago es obligatorio"
        )

        @Size(
                max = 80,
                message = "El nombre del medio de pago no puede superar los 80 caracteres"
        )

        @Pattern(
                regexp = "^(?=.*\\p{L})[\\p{L}\\p{N}\\s°ºª.\\-/]+$",
                message = "El nombre debe contener al menos una letra y no puede contener símbolos inválidos"
        )

        String nombre

) {
}