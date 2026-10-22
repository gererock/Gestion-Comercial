package gestion_comercial.dto.response;

import java.time.LocalDateTime;


public record ClienteCreacionResponse(

        Integer idCliente,

        String nombre,

        String apellido,

        Boolean estado,

        LocalDateTime fechaAlta,

        String mensaje

) {
}