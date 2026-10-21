package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.PedidoDecisionResponse;

public interface IPedidoDecisionService {

    PedidoDecisionResponse aprobar(Integer idPedido, String emailUsuario);

    PedidoDecisionResponse cancelar(Integer idPedido, String emailUsuario);
}
