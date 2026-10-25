package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.NotaInternaClienteRequest;
import gestion_comercial.dto.response.NotaInternaClienteResponse;

public interface INotaInternaClienteService {

    NotaInternaClienteResponse actualizar(
            Integer idCliente,
            NotaInternaClienteRequest request
    );
}
