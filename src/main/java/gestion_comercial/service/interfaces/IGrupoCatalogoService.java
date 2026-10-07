package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.GrupoCatalogoCreateRequest;
import gestion_comercial.dto.request.GrupoCatalogoUpdateRequest;
import gestion_comercial.dto.response.GrupoCatalogoResponse;

import java.util.List;

public interface IGrupoCatalogoService {

    GrupoCatalogoResponse crear(
            GrupoCatalogoCreateRequest request
    );

    List<GrupoCatalogoResponse> listar();

    GrupoCatalogoResponse buscarPorId(
            Integer id
    );

    GrupoCatalogoResponse actualizar(
            Integer id,
            GrupoCatalogoUpdateRequest request
    );

    GrupoCatalogoResponse cambiarEstado(
            Integer id,
            Boolean activo
    );
}