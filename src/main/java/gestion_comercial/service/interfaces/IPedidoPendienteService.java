package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.PedidoPendienteResponse;

import java.util.List;


public interface IPedidoPendienteService {


    List<PedidoPendienteResponse>
    listarPendientes();


    PedidoPendienteResponse
    buscarPendiente(
            Integer idPedido
    );
}