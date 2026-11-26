package gestion_comercial.dto.response;

import java.math.BigDecimal;

public record ClienteFrecuenteResponse(

        Integer idCliente,

        String nombre,

        String apellido,

        String razonSocial,

        String telefono,

        String correo,

        long cantidadCompras,

        BigDecimal totalComprado

) {
}