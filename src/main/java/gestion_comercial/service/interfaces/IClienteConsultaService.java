package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.ClienteConsultaResponse;

import java.util.List;


public interface IClienteConsultaService {


    List<ClienteConsultaResponse>
    listar(
            String busqueda
    );


    ClienteConsultaResponse
    buscarPorId(
            Integer idCliente
    );
}