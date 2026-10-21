
package gestion_comercial.dto.response;

import gestion_comercial.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConfirmarPedidoResponse(

        Integer idPedido,
        String numeroPedido,
        EstadoPedido estadoPedido,
        EstadoPago estadoPago,
        MedioEntrega medioEntrega,
        LocalidadEnvio localidadEnvio,
        BigDecimal subtotal,
        BigDecimal descuentoOferta,
        BigDecimal descuentoMayorista,
        BigDecimal descuentoCliente,
        BigDecimal total,
        LocalDateTime fechaCreacion

) {
}
