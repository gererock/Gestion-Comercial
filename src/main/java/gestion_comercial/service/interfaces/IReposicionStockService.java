package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.ReposicionStockRequest;
import gestion_comercial.dto.response.ReposicionStockResponse;

public interface IReposicionStockService {

    ReposicionStockResponse registrar(
            ReposicionStockRequest request,
            String emailUsuario
    );
}
