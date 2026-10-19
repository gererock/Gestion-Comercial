package gestion_comercial.dto.response;
import gestion_comercial.entity.LocalidadEnvio;

public record DireccionEnvioResponse(

    Integer idDireccionEnvio,

    String calle,

    String numero,

    String pisoDepartamento,

    LocalidadEnvio localidad,

    String referencia,

    Boolean habilitadaParaEnvio

) {
}