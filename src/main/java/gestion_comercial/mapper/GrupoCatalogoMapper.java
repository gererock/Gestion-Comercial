package gestion_comercial.mapper;

import gestion_comercial.dto.request.GrupoCatalogoCreateRequest;
import gestion_comercial.dto.request.GrupoCatalogoUpdateRequest;
import gestion_comercial.dto.response.GrupoCatalogoResponse;
import gestion_comercial.entity.GrupoCatalogo;

public class GrupoCatalogoMapper {

    private GrupoCatalogoMapper() {
    }


    public static GrupoCatalogo toEntity(
            GrupoCatalogoCreateRequest request
    ) {

        GrupoCatalogo grupo =
                new GrupoCatalogo();


        grupo.setNombreVisible(
                request.nombreVisible()
        );

        grupo.setDescripcion(
                request.descripcion()
        );

        grupo.setImagenUrl(
                request.imagenUrl()
        );

        grupo.setActivo(
                true
        );


        return grupo;
    }


    public static void updateEntity(
            GrupoCatalogo grupo,
            GrupoCatalogoUpdateRequest request
    ) {

        grupo.setNombreVisible(
                request.nombreVisible()
        );

        grupo.setDescripcion(
                request.descripcion()
        );

        grupo.setImagenUrl(
                request.imagenUrl()
        );
    }


    public static GrupoCatalogoResponse toResponse(
            GrupoCatalogo grupo
    ) {

        return new GrupoCatalogoResponse(

                grupo.getIdGrupoCatalogo(),

                grupo.getNombreVisible(),

                grupo.getDescripcion(),

                grupo.getImagenUrl(),

                grupo.getActivo()

        );
    }
}