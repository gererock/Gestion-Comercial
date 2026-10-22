package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.ClienteCreacionRequest;
import gestion_comercial.dto.response.ClienteCreacionResponse;


public interface IClienteCreacionService {


    ClienteCreacionResponse crear(
            ClienteCreacionRequest request
    );
}