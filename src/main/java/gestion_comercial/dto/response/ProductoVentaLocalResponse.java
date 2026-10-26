package gestion_comercial.dto.response;
import gestion_comercial.entity.Producto;
import java.math.BigDecimal;
public record ProductoVentaLocalResponse(Integer idProducto, String nombre, Integer stockActual,
 BigDecimal precioVenta, Boolean enOferta, BigDecimal porcentajeDescuentoOferta,
 Integer cantidadMinimaMayorista, BigDecimal porcentajeDescuentoMayorista) {
 public static ProductoVentaLocalResponse de(Producto p) {
 return new ProductoVentaLocalResponse(p.getIdProducto(),p.getNombre(),p.getStockActual(),p.getPrecioVenta(),
 p.getEnOferta(),p.getPorcentajeDescuentoOferta(),p.getCantidadMinimaMayorista(),p.getPorcentajeDescuentoMayorista());
 }
}
