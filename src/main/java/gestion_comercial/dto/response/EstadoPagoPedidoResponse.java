package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;

import java.time.LocalDateTime;


public record EstadoPagoPedidoResponse(

        Integer idPedido,

        String numeroPedido,

        EstadoPedido estadoPedido,

        EstadoPago estadoPago,

        String descripcionPago,

        Integer idMedioPago,

        String medioPago,

        LocalDateTime fechaActualizacion

) {
}