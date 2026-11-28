package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.PedidoPendienteResponse;
import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.MedioEntrega;

import java.util.List;


public interface IPedidoPendienteService {

    List<PedidoPendienteResponse> listarPendientes();

    List<PedidoPendienteResponse>
    listarPendientes(
            String busqueda,
            EstadoPedido estadoPedido,
            MedioEntrega medioEntrega,
            EstadoPago estadoPago
    );


    PedidoPendienteResponse
    buscarPendiente(
            Integer idPedido
    );
}
