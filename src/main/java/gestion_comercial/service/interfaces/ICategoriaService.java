package gestion_comercial.service.interfaces;

import java.util.List;

import gestion_comercial.dto.request.CategoriaCreateRequest;
import gestion_comercial.dto.request.CategoriaUpdateRequest;
import gestion_comercial.dto.response.CategoriaResponse;

public interface ICategoriaService {
    CategoriaResponse crear(CategoriaCreateRequest request);

    CategoriaResponse buscarPorId(Integer id);

    CategoriaResponse actualizar(Integer id, CategoriaUpdateRequest request);

    CategoriaResponse cambiarEstado(Integer id, Boolean activo);

    List<CategoriaResponse> buscar(String nombre, Boolean activa);
  
}
