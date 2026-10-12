package gestion_comercial.dto.response;


public record ImagenProductoResponse(

        Integer idImagen,

        Integer idProducto,

        String urlImagen,

        Integer orden,

        Boolean principal

) {
}