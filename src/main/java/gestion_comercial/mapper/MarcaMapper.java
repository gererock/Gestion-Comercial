package gestion_comercial.mapper;

import gestion_comercial.dto.request.MarcaCreateRequest;
import gestion_comercial.dto.response.MarcaResponse;
import gestion_comercial.entity.Marca;

public class MarcaMapper {

    public static Marca toEntity(
            MarcaCreateRequest request) {

        Marca marca = new Marca();

        marca.setNombre(request.nombre());
        marca.setActiva(true);

        return marca;
    }

    public static MarcaResponse toResponse(
            Marca marca) {

        return new MarcaResponse(
            marca.getId(),
            marca.getNombre(),
            marca.getActiva()
        );
    }
}