package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.PedidoDetalleResponse;

public interface IPedidoDetalleService {

    PedidoDetalleResponse buscarDetalle(Integer idPedido);
}
