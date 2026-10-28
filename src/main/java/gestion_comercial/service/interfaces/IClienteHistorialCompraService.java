package gestion_comercial.service.interfaces;

import java.util.List;

import gestion_comercial.dto.response.ClienteHistorialCompraDetalleResponse;
import gestion_comercial.dto.response.ClienteHistorialCompraResumenResponse;

public interface IClienteHistorialCompraService {

    List<ClienteHistorialCompraResumenResponse> listar(Integer idCliente);

    ClienteHistorialCompraDetalleResponse buscarDetalle(Integer idCliente, Integer idPedido);
}
