package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.LocalidadEnvio;
import gestion_comercial.entity.MedioEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoClienteDetalleResponse(
        Integer idPedido,
        String numeroPedido,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        EstadoPedido estadoPedido,
        EstadoPago estadoPago,
        String medioPago,
        MedioEntrega medioEntrega,
        LocalidadEnvio localidadEnvio,
        String direccionEnvio,
        List<PedidoClienteProductoResponse> productos,
        BigDecimal subtotal,
        BigDecimal descuentoCliente,
        BigDecimal descuentoMayorista,
        BigDecimal descuentoOferta,
        BigDecimal total,
        String observacionesCliente
) {
}
