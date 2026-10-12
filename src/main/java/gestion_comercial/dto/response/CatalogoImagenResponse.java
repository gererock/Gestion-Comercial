package gestion_comercial.dto.response;


public record CatalogoImagenResponse(

        String urlImagen,

        Boolean principal,

        Integer orden

) {
}