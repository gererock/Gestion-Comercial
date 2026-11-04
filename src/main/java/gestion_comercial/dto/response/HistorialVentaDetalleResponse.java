package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.MedioEntrega;
import gestion_comercial.entity.TipoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public record HistorialVentaDetalleResponse(

        Integer idPedido,

        String numeroPedido,

        LocalDateTime fecha,

        String cliente,

        TipoVenta tipoVenta,

        EstadoPedido estadoPedido,

        EstadoPago estadoPago,

        String medioPago,

        MedioEntrega medioEntrega,

        List<HistorialVentaProductoResponse> productos,

        BigDecimal subtotal,

        BigDecimal descuentoCliente,

        BigDecimal descuentoMayorista,

        BigDecimal descuentoOferta,

        BigDecimal total

) {
}