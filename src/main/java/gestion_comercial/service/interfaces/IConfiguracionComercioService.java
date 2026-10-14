package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.MontoMinimoEnvioRequest;
import gestion_comercial.dto.response.ConfiguracionEnvioResponse;


public interface IConfiguracionComercioService {

    ConfiguracionEnvioResponse obtenerConfiguracionEnvio();


    ConfiguracionEnvioResponse actualizarMontoMinimoEnvio(
            MontoMinimoEnvioRequest request
    );
}