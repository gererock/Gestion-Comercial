package gestion_comercial.dto.response;

import java.math.BigDecimal;


public record HistorialVentaProductoResponse(

        Integer idProducto,

        String producto,

        Integer cantidad,

        BigDecimal precioUnitario,

        BigDecimal descuentoAplicado,

        BigDecimal subtotal

) {
}