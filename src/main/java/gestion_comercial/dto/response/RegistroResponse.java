package gestion_comercial.dto.response;

public record RegistroResponse(
    Integer idUsuario,
    Integer IdCliente,
    String nombre,
    String apellido,
    String email,
    String rol, 
    String mensaje
) {

}
