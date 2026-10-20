package gestion_comercial.dto.request;

import gestion_comercial.entity.MedioEntrega;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ConfirmarPedidoRequest(

        @NotNull(message = "La clave de confirmación es obligatoria")
        UUID claveConfirmacion,

        @NotEmpty(message = "El carrito no puede estar vacío")
        List<@NotNull @Valid ItemConfirmarPedidoRequest> productos,

        @NotNull(message = "El medio de pago es obligatorio")
        @Positive(message = "El medio de pago debe ser válido")
        Integer idMedioPago,

        @NotNull(message = "La modalidad de entrega es obligatoria")
        MedioEntrega medioEntrega,

        @Positive(message = "La dirección debe ser válida")
        Integer idDireccionEnvio,

        @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
        String observacionesCliente

) {
}