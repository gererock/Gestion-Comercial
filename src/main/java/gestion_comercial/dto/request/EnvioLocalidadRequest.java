package gestion_comercial.dto.request;

import jakarta.validation.constraints.NotNull;

public record EnvioLocalidadRequest(

        @NotNull(
                message = "Debe indicar si Villa María está habilitada para envío"
        )
        Object permiteEnvioVillaMaria,

        @NotNull(
                message = "Debe indicar si Villa Nueva está habilitada para envío"
        )
        Object permiteEnvioVillaNueva

) {
}