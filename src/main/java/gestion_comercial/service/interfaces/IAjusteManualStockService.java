package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.AjusteManualStockRequest;
import gestion_comercial.dto.response.AjusteManualStockResponse;

public interface IAjusteManualStockService {

    AjusteManualStockResponse ajustar(
            AjusteManualStockRequest request,
            String emailUsuario
    );
}
