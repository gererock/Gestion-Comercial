package gestion_comercial.dto.response;


public record MedioPagoResponse(

        Integer idMedioPago,

        String nombre,

        Boolean activo

) {
}