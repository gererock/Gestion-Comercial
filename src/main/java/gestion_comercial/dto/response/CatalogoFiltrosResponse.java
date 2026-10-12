package gestion_comercial.dto.response;

import java.util.List;


public record CatalogoFiltrosResponse(

        List<CatalogoFiltroOpcionResponse> categorias,

        List<CatalogoFiltroOpcionResponse> marcas

) {
}