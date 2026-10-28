package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.MedioEntrega;
import gestion_comercial.entity.TipoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ClienteHistorialCompraDetalleResponse(
        Integer idPedido,
        String numeroPedido,
        LocalDateTime fechaCreacion,
        TipoVenta tipoVenta,
        EstadoPedido estadoPedido,
        EstadoPago estadoPago,
        MedioEntrega medioEntrega,
        String medioPago,
        List<ClienteHistorialCompraProductoResponse> productos,
        BigDecimal subtotal,
        BigDecimal descuentoOferta,
        BigDecimal descuentoMayorista,
        BigDecimal descuentoCliente,
        BigDecimal total
) {
}