package gestion_comercial.service.impl;

import gestion_comercial.dto.response.PedidoClienteResumenResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.TipoVenta;
import gestion_comercial.exception.PedidoNoEncontradoException;
import gestion_comercial.repository.ClienteRepository;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.service.interfaces.IPedidoClienteConsultaService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoClienteConsultaService
        implements IPedidoClienteConsultaService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PedidoClienteResumenResponse> listar(String emailCliente) {
        if (emailCliente == null || emailCliente.isBlank()) {
            throw new PedidoNoEncontradoException(
                    "No existe un cliente asociado a la sesión"
            );
        }

        Cliente cliente = clienteRepository
                .findByUsuario_EmailIgnoreCase(emailCliente)
                .orElseThrow(() -> new PedidoNoEncontradoException(
                        "No existe un cliente asociado a la sesión"
                ));

        return pedidoRepository
                .findByCliente_IdClienteAndTipoVentaOrderByFechaCreacionDesc(
                        cliente.getIdCliente(),
                        TipoVenta.ONLINE
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    private PedidoClienteResumenResponse convertir(Pedido pedido) {
        return new PedidoClienteResumenResponse(
                pedido.getIdPedido(),
                pedido.getNumeroPedido(),
                pedido.getFechaCreacion(),
                pedido.getFechaActualizacion(),
                pedido.getEstadoPedido(),
                pedido.getEstadoPago(),
                pedido.getMedioEntrega(),
                pedido.getTotal()
        );
    }
}
