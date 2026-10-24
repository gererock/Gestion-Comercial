package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.ClienteUsuarioAsociacionRequest;
import gestion_comercial.dto.response.ClienteAsociacionResponse;
import gestion_comercial.dto.response.ClienteUsuarioAsociacionResponse;
import gestion_comercial.dto.response.UsuarioAsociacionResponse;

import java.util.List;

public interface IClienteUsuarioAsociacionService {

    List<ClienteAsociacionResponse> buscarClientes(String busqueda);

    List<UsuarioAsociacionResponse> buscarUsuarios(String busqueda);

    ClienteUsuarioAsociacionResponse asociar(
            ClienteUsuarioAsociacionRequest request
    );
}
