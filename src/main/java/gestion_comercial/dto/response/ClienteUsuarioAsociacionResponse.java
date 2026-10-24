package gestion_comercial.dto.response;

public record ClienteUsuarioAsociacionResponse(
        Integer idCliente,
        String nombreCliente,
        String apellidoCliente,
        Integer idUsuario,
        String nombreUsuario,
        String apellidoUsuario,
        String emailUsuario,
        String mensaje
) {
}
