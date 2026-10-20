package gestion_comercial.dto.response;

public record PedidoDetalleClienteResponse(
        Integer idCliente,
        String nombre,
        String apellido,
        String razonSocial,
        String dni,
        String cuit,
        String telefono,
        String correo
) {
}
