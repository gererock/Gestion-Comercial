package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.PedidoClienteResumenResponse;

import java.util.List;

public interface IPedidoClienteConsultaService {

    List<PedidoClienteResumenResponse> listar(String emailCliente);
}
