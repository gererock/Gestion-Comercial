package gestion_comercial.dto.response;

public record CategoriaGastoResponse(

        Integer idCategoriaGasto,

        String nombre,

        String descripcion,

        Boolean activa

) {
}