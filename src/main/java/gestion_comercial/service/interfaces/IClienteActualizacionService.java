package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.ClienteActualizacionRequest;
import gestion_comercial.dto.response.ClienteActualizacionResponse;

public interface IClienteActualizacionService {

    ClienteActualizacionResponse actualizar(
            Integer idCliente,
            ClienteActualizacionRequest request
    );
}
