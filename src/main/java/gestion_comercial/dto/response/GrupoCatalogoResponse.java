package gestion_comercial.dto.response;

public record GrupoCatalogoResponse(

        Integer idGrupoCatalogo,

        String nombreVisible,

        String descripcion,

        String imagenUrl,

        Boolean activo

) {
}