package gestion_comercial.dto.request;

public record GrupoCatalogoCreateRequest(

        String nombreVisible,

        String descripcion,

        String imagenUrl

) {
}