package gestion_comercial.dto.response;

public record NotaInternaClienteResponse(
        Integer idCliente,
        String nombre,
        String apellido,
        String notaInterna,
        String mensaje
) {
}
