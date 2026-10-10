package gestion_comercial.service.interfaces;

import java.util.List;

import gestion_comercial.dto.request.MarcaCreateRequest;
import gestion_comercial.dto.request.MarcaUpdateRequest;
import gestion_comercial.dto.response.MarcaResponse;

public interface IMarcaService {
    MarcaResponse crear(MarcaCreateRequest request);

    List<MarcaResponse> obtenerTodo();

    MarcaResponse actualizar(
            Integer id,
            MarcaUpdateRequest request);

    MarcaResponse cambiarEstado(Integer id, Boolean activa);

    List<MarcaResponse> buscar(String nombre, Boolean activa);

    
}
