package gestion_comercial.dto.response;

public record UsuarioAsociacionResponse(
        Integer idUsuario,
        String nombre,
        String apellido,
        String email,
        String rol,
        String estado,
        Boolean asociado,
        Integer idClienteAsociado
) {
}
