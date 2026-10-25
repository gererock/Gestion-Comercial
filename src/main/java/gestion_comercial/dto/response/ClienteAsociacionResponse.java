package gestion_comercial.dto.response;

public record ClienteAsociacionResponse(
        Integer idCliente,
        String nombre,
        String apellido,
        String dni,
        String correo,
        Boolean estado,
        Boolean asociado,
        Integer idUsuarioAsociado,
        String emailUsuarioAsociado
) {
}
