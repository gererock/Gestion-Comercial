package gestion_comercial.mapper;

import gestion_comercial.dto.request.CategoriaGastoCreateRequest;
import gestion_comercial.dto.response.CategoriaGastoResponse;
import gestion_comercial.entity.CategoriaGasto;

public class CategoriaGastoMapper {

    public static CategoriaGasto toEntity(
            CategoriaGastoCreateRequest request
    ) {

        CategoriaGasto categoria =
                new CategoriaGasto();


        categoria.setNombre(
                request.nombre()
        );


        categoria.setDescripcion(
                request.descripcion()
        );


        categoria.setActiva(
                true
        );


        return categoria;
    }


    public static CategoriaGastoResponse toResponse(
            CategoriaGasto categoria
    ) {

        return new CategoriaGastoResponse(
                categoria.getIdCategoriaGasto(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getActiva()
        );
    }
}