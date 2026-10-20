package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.ActualizarEstadoPedidoRequest;
import gestion_comercial.dto.response.PedidoEstadoResponse;

public interface IPedidoEstadoService {

    PedidoEstadoResponse actualizar(
            Integer idPedido,
            ActualizarEstadoPedidoRequest request
    );
}
