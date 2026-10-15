package gestion_comercial.dto.request;

import gestion_comercial.entity.EstadoPago;

import jakarta.validation.constraints.NotNull;


public record EstadoPagoPedidoRequest(

        @NotNull(
                message = "El estado de pago es obligatorio"
        )
        EstadoPago estadoPago,


        String descripcionPago

) {
}