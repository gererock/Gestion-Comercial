package gestion_comercial.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;


public record MontoMinimoEnvioRequest(

        @NotNull(
                message = "El monto mínimo de envío es obligatorio"
        )

        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "El monto mínimo de envío no puede ser negativo"
        )

        @Digits(
                integer = 10,
                fraction = 2,
                message = "El monto mínimo de envío debe tener como máximo 10 enteros y 2 decimales"
        )

        BigDecimal montoMinimoEnvio

) {
}