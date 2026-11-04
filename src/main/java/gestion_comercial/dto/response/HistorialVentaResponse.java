package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.MedioEntrega;
import gestion_comercial.entity.TipoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record HistorialVentaResponse(

        Integer idPedido,

        String numeroPedido,

        LocalDateTime fecha,

        String cliente,

        TipoVenta tipoVenta,

        EstadoPedido estadoPedido,

        EstadoPago estadoPago,

        String medioPago,

        MedioEntrega medioEntrega,

        BigDecimal total

) {
}