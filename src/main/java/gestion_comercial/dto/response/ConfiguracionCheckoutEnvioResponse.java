package gestion_comercial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConfiguracionCheckoutEnvioResponse(
    BigDecimal montoMinimoEnvio,

    Boolean permiteEnvioVillaMaria,

    Boolean permiteEnvioVillaNueva,

    LocalDateTime fechaActualizacion
) {

}
