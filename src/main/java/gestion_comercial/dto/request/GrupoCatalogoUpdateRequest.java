package gestion_comercial.dto.request;

public record GrupoCatalogoUpdateRequest(

        String nombreVisible,

        String descripcion,

        String imagenUrl

) {
}