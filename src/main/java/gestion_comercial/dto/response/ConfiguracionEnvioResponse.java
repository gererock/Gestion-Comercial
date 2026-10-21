package gestion_comercial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record ConfiguracionEnvioResponse(

        BigDecimal montoMinimoEnvio,

        LocalDateTime fechaActualizacion

) {
}