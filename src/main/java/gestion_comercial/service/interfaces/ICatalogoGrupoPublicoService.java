package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.CatalogoOpcionGrupoResponse;

import java.util.List;


public interface ICatalogoGrupoPublicoService {

    List<CatalogoOpcionGrupoResponse> obtenerOpciones(
            Integer idGrupoCatalogo
    );

}