package gestion_comercial.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ClienteUsuarioAsociacionRequest(

        @NotNull(message = "Debe seleccionar un cliente")
        @Positive(message = "El cliente seleccionado no es válido")
        Integer idCliente,

        @NotNull(message = "Debe seleccionar un usuario")
        @Positive(message = "El usuario seleccionado no es válido")
        Integer idUsuario
) {
}
