package gestion_comercial.service.impl;

import gestion_comercial.dto.response.ClienteHistorialCompraDetalleResponse;
import gestion_comercial.dto.response.ClienteHistorialCompraProductoResponse;
import gestion_comercial.dto.response.ClienteHistorialCompraResumenResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.DetallePedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.exception.ClienteNoEncontradoException;
import gestion_comercial.exception.PedidoNoEncontradoException;
import gestion_comercial.repository.ClienteRepository;
import gestion_comercial.repository.DetallePedidoRepository;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.service.interfaces.IClienteHistorialCompraService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteHistorialCompraService
        implements IClienteHistorialCompraService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteHistorialCompraResumenResponse> listar(
            Integer idCliente) {

        Cliente cliente = obtenerCliente(idCliente);

        return pedidoRepository
                .findByCliente_IdClienteOrderByFechaCreacionDescIdPedidoDesc(
                        cliente.getIdCliente())
                .stream()
                .map(this::convertirResumen)
                .toList();
    }



    private Cliente obtenerCliente(
            Integer idCliente) {

        if (idCliente == null || idCliente <= 0) {
            throw new ClienteNoEncontradoException(
                    "El cliente indicado no existe");
        }

        return clienteRepository
                .findById(idCliente)
                .orElseThrow(
                        () -> new ClienteNoEncontradoException(
                                "El cliente indicado no existe"));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteHistorialCompraDetalleResponse buscarDetalle(
            Integer idCliente,
            Integer idPedido) {

        Cliente cliente = obtenerCliente(idCliente);

        if (idPedido == null || idPedido <= 0) {
            throw new PedidoNoEncontradoException(
                    "La compra indicada no existe o no pertenece al cliente seleccionado");
        }

        Pedido pedido = pedidoRepository
                .findByIdPedidoAndCliente_IdCliente(
                        idPedido,
                        cliente.getIdCliente())
                .orElseThrow(
                        () -> new PedidoNoEncontradoException(
                                "La compra indicada no existe o no pertenece al cliente seleccionado"));

        List<ClienteHistorialCompraProductoResponse> productos = detallePedidoRepository
                .findByPedido_IdPedidoOrderByIdDetallePedidoAsc(
                        pedido.getIdPedido())
                .stream()
                .map(this::convertirProducto)
                .toList();

        String medioPago = pedido.getMedioPago() == null
                ? null
                : pedido.getMedioPago().getNombre();

        return new ClienteHistorialCompraDetalleResponse(
                pedido.getIdPedido(),
                pedido.getNumeroPedido(),
                pedido.getFechaCreacion(),
                pedido.getTipoVenta(),
                pedido.getEstadoPedido(),
                pedido.getEstadoPago(),
                pedido.getMedioEntrega(),
                medioPago,
                productos,
                pedido.getSubtotal(),
                pedido.getDescuentoOferta(),
                pedido.getDescuentoMayorista(),
                pedido.getDescuentoCliente(),
                pedido.getTotal());
    }

    private ClienteHistorialCompraProductoResponse convertirProducto(
            DetallePedido detalle) {

        return new ClienteHistorialCompraProductoResponse(
                detalle.getProducto().getIdProducto(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getDescuentoAplicado(),
                detalle.getSubtotal());
    }

    private ClienteHistorialCompraResumenResponse convertirResumen(
            Pedido pedido) {

        return new ClienteHistorialCompraResumenResponse(
                pedido.getIdPedido(),
                pedido.getNumeroPedido(),
                pedido.getFechaCreacion(),
                pedido.getTipoVenta(),
                pedido.getEstadoPedido(),
                pedido.getEstadoPago(),
                pedido.getMedioEntrega(),
                pedido.getTotal());
    }
}