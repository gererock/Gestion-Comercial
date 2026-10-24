package gestion_comercial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClienteActualizacionResponse(
        Integer idCliente,
        String nombre,
        String apellido,
        String dni,
        String telefono,
        String correo,
        Boolean estado,
        String razonSocial,
        String cuit,
        String condicionIva,
        String direccionFacturacion,
        String localidadFacturacion,
        String provinciaFacturacion,
        BigDecimal descuentoPorcentaje,
        String notaInterna,
        LocalDateTime fechaAlta,
        String mensaje
) {
}
