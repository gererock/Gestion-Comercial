package gestion_comercial.dto.response;

public record LoginResponse(
        Integer idUsuario,
        String nombre,
        String apellido,
        String email,
        String mensaje
) {
}