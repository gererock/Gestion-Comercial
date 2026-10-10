package gestion_comercial.dto.response;

public record CategoriaResponse(
    Integer id,

    String nombre,

    String descripcion,

    Boolean activa
) {

}
