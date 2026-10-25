package gestion_comercial.dto.request;

import jakarta.validation.constraints.Size;

public record NotaInternaClienteRequest(

        @Size(
                max = 1000,
                message = "La nota interna no puede superar los 1000 caracteres"
        )
        String notaInterna
) {
}
