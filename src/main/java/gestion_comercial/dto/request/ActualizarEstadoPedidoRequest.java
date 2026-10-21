package gestion_comercial.dto.request;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ActualizarEstadoPedidoRequest(
        @NotBlank(message = "El nuevo estado es obligatorio")
        @Pattern(
                regexp = "PENDIENTE_APROBACION|CONFIRMADO|EN_PREPARACION|LISTO_PARA_RETIRAR|LISTO_PARA_ENVIAR|ENVIADO|ENTREGADO|CANCELADO",
                message = "El estado indicado no es válido"
        )
        String estadoPedido
) {

    @JsonAnySetter
    public void rechazarCampoDesconocido(String nombre, Object valor) {
        throw new IllegalArgumentException(
                "El campo '" + nombre + "' no está permitido"
        );
    }
}
