package gestion_comercial.dto.response;

import java.time.LocalDateTime;

public record EnvioLocalidadResponse(
    Boolean permiteEnvioVillaMaria,

    Boolean permiteEnvioVillaNueva,

    LocalDateTime fechaActualizacion
) {

}
