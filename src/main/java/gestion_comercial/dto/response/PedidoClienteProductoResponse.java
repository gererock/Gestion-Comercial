package gestion_comercial.dto.response;

import java.math.BigDecimal;

public record PedidoClienteProductoResponse(
        Integer idProducto,
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal descuentoAplicado,
        BigDecimal subtotal
) {
}
