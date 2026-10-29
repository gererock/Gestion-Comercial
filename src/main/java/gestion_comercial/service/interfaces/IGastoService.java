package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.GastoCreateRequest;
import gestion_comercial.dto.response.GastoResponse;

public interface IGastoService {

    GastoResponse crear (GastoCreateRequest request, String emailUsuario);

}
