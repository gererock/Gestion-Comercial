package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.CategoriaGastoCreateRequest;
import gestion_comercial.dto.request.CategoriaGastoUpdateRequest;
import gestion_comercial.dto.response.CategoriaGastoResponse;

import java.util.List;

public interface ICategoriaGastoService {

    CategoriaGastoResponse crear(
            CategoriaGastoCreateRequest request
    );

    CategoriaGastoResponse buscarPorId(
            Integer idCategoriaGasto
    );

    CategoriaGastoResponse actualizar(
            Integer idCategoriaGasto,
            CategoriaGastoUpdateRequest request
    );

    CategoriaGastoResponse cambiarEstado(
            Integer idCategoriaGasto,
            Boolean activa
    );

    List<CategoriaGastoResponse> buscar(
            String nombre,
            Boolean activa
    );
}