package gestion_comercial.dto.response;

public record MarcaResponse(
    Integer id_marca,
    String nombre,
    Boolean activa
) {
}