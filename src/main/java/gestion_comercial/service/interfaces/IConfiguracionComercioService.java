package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.EnvioLocalidadRequest;
import gestion_comercial.dto.request.MontoMinimoEnvioRequest;
import gestion_comercial.dto.response.ConfiguracionCheckoutEnvioResponse;
import gestion_comercial.dto.response.ConfiguracionEnvioResponse;
import gestion_comercial.dto.response.EnvioLocalidadResponse;


public interface IConfiguracionComercioService {

    ConfiguracionEnvioResponse obtenerConfiguracionEnvio();


    ConfiguracionEnvioResponse actualizarMontoMinimoEnvio(
            MontoMinimoEnvioRequest request
    );

    EnvioLocalidadResponse obtenerLocalidades();

    EnvioLocalidadResponse actualizarLocalidades(EnvioLocalidadRequest request);

    ConfiguracionCheckoutEnvioResponse obtenerCheckout();
}