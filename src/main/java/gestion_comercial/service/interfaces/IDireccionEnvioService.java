package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.DireccionEnvioRequest;
import gestion_comercial.dto.response.DireccionEnvioResponse;

import java.util.List;


public interface IDireccionEnvioService {

    List<DireccionEnvioResponse> listar(
            String emailUsuario
    );


    DireccionEnvioResponse registrar(
            String emailUsuario,
            DireccionEnvioRequest request
    );


    DireccionEnvioResponse buscarPropia(
            String emailUsuario,
            Integer idDireccionEnvio
    );
}