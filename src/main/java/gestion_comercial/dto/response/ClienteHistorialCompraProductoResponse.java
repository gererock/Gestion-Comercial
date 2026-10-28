package gestion_comercial.dto.response;

import java.math.BigDecimal;

public record ClienteHistorialCompraProductoResponse(
        Integer idProducto,
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal descuentoAplicado,
        BigDecimal subtotal
) {
}