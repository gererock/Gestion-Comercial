package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.LocalidadEnvio;
import gestion_comercial.entity.MedioEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoClienteResumenResponse(
        Integer idPedido,
        String numeroPedido,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        EstadoPedido estadoPedido,
        EstadoPago estadoPago,
        MedioEntrega medioEntrega,
        LocalidadEnvio localidadEnvio,
        String direccionEnvio,
        BigDecimal total
) {
}
