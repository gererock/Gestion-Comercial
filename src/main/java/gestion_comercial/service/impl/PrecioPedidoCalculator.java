package gestion_comercial.service.impl;
import gestion_comercial.entity.Producto;
import gestion_comercial.exception.PedidoConfirmacionException;
import java.math.BigDecimal;
import java.math.RoundingMode;
/** Cálculo compartido por confirmación online y venta local. */
public final class PrecioPedidoCalculator {
 private PrecioPedidoCalculator() {}
 private static final BigDecimal CIEN = new BigDecimal("100");
 private static final BigDecimal MAX_IMPORTE = new BigDecimal("9999999999.99");
    public static Linea calcularLinea(Producto producto, int cantidad) {

        BigDecimal precio = producto.getPrecioVenta();

        if (precio == null
                || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PedidoConfirmacionException(
                    "El precio del producto no es válido."
            );
        }

        BigDecimal bruto = dinero(
                precio.multiply(BigDecimal.valueOf(cantidad))
        );

        BigDecimal oferta = BigDecimal.ZERO;
        BigDecimal mayorista = BigDecimal.ZERO;

        // La oferta tiene prioridad, igual que en el carrito.
        if (Boolean.TRUE.equals(producto.getEnOferta())) {

            oferta = descuento(bruto, producto.getPorcentajeDescuentoOferta());

        } else if (producto.getCantidadMinimaMayorista() != null
                && producto.getPorcentajeDescuentoMayorista() != null
                && cantidad >= producto.getCantidadMinimaMayorista()) {

            mayorista = descuento(
                    bruto,
                    producto.getPorcentajeDescuentoMayorista()
            );
        }

        BigDecimal neto = dinero(
                bruto.subtract(oferta).subtract(mayorista)
        );

        validarImporte(bruto);
        validarImporte(neto);

        return new Linea(
                producto, cantidad, bruto, oferta, mayorista, neto
        );
    }

    private static BigDecimal descuento(BigDecimal monto, BigDecimal porcentaje) {

        if (porcentaje == null
                || porcentaje.compareTo(BigDecimal.ZERO) < 0
                || porcentaje.compareTo(CIEN) > 0) {
            throw new PedidoConfirmacionException(
                    "El descuento configurado no es válido."
            );
        }

        return dinero(
                monto.multiply(porcentaje)
                        .divide(CIEN, 6, RoundingMode.HALF_UP)
        );
    }

    private static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static void validarImporte(BigDecimal importe) {
        if (importe.compareTo(BigDecimal.ZERO) < 0
                || importe.compareTo(MAX_IMPORTE) > 0) {
            throw new PedidoConfirmacionException(
                    "El importe del pedido supera los límites permitidos."
            );
        }
    }

    public record Linea(
            Producto producto,
            int cantidad,
            BigDecimal bruto,
            BigDecimal oferta,
            BigDecimal mayorista,
            BigDecimal neto
    ) {
    }
}
