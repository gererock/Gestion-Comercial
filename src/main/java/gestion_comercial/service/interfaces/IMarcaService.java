package gestion_comercial.service.interfaces;

import java.util.List;

import gestion_comercial.dto.request.MarcaCreateRequest;
import gestion_comercial.dto.response.MarcaResponse;

public interface IMarcaService {
    MarcaResponse crear(MarcaCreateRequest request);

    List<MarcaResponse> obtenerTodo();
}
