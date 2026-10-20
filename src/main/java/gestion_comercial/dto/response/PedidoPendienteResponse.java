package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.LocalidadEnvio;
import gestion_comercial.entity.MedioEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record PedidoPendienteResponse(

        Integer idPedido,

        String numeroPedido,

        Integer idCliente,

        String cliente,

        LocalDateTime fechaCreacion,

        EstadoPedido estadoPedido,

        EstadoPago estadoPago,

        MedioEntrega medioEntrega,

        LocalidadEnvio localidadEnvio,

        String direccionEnvio,

        BigDecimal total

) {
}