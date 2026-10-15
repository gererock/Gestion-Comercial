package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.EstadoPagoPedidoRequest;
import gestion_comercial.dto.response.EstadoPagoPedidoResponse;


public interface IPedidoPagoService {

    EstadoPagoPedidoResponse consultar(
            Integer idPedido
    );


    EstadoPagoPedidoResponse actualizar(
            Integer idPedido,
            EstadoPagoPedidoRequest request
    );
}