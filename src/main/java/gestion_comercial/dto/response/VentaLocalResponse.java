package gestion_comercial.dto.response;
import gestion_comercial.entity.*;
import java.math.BigDecimal;
import java.util.List;
public record VentaLocalResponse(Integer idPedido, String numeroPedido, EstadoPedido estadoPedido,
 EstadoPago estadoPago, BigDecimal subtotal, BigDecimal descuentoOferta, BigDecimal descuentoMayorista,
 BigDecimal total, List<Item> items) {
 public record Item(Integer idProducto, String nombre, Integer cantidad, BigDecimal precioUnitario,
 BigDecimal descuentoAplicado, BigDecimal subtotal) {}
}
