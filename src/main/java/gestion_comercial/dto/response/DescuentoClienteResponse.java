package gestion_comercial.dto.response;

import java.math.BigDecimal;

public record DescuentoClienteResponse(
        Integer idCliente,
        String nombre,
        String apellido,
        BigDecimal descuentoPorcentaje,
        String mensaje
) {
}
