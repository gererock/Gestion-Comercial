package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.DescuentoClienteRequest;
import gestion_comercial.dto.response.DescuentoClienteResponse;

public interface IDescuentoClienteService {

    DescuentoClienteResponse actualizar(
            Integer idCliente,
            DescuentoClienteRequest request
    );
}
