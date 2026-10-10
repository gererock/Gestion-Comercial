package gestion_comercial.mapper;

import gestion_comercial.dto.request.CategoriaCreateRequest;
import gestion_comercial.dto.response.CategoriaResponse;
import gestion_comercial.entity.Categoria;

public class CategoriaMapper {
    public static Categoria toEntity(CategoriaCreateRequest request) {
        Categoria categoria = new Categoria();

        categoria.setNombre(request.nombre());
        categoria.setDescripcion(request.descripcion());
        categoria.setActiva(true);
        return categoria;
    }

    public static CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
            categoria.getId(),
            categoria.getNombre(),
            categoria.getDescripcion(),
            categoria.getActiva()
        );
    }

}
