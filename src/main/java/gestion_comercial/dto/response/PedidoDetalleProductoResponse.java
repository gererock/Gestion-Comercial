package gestion_comercial.dto.response;

import java.math.BigDecimal;

public record PedidoDetalleProductoResponse(
        Integer idProducto,
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal descuentoAplicado,
        BigDecimal subtotal,
        Integer stockDisponible,
        Boolean stockSuficiente
) {
}
