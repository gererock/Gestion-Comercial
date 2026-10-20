package gestion_comercial.service.impl;

import gestion_comercial.dto.response.PedidoClienteDetalleResponse;
import gestion_comercial.dto.response.PedidoClienteProductoResponse;
import gestion_comercial.dto.response.PedidoClienteResumenResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.DetallePedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.TipoVenta;
import gestion_comercial.exception.PedidoNoEncontradoException;
import gestion_comercial.repository.ClienteRepository;
import gestion_comercial.repository.DetallePedidoRepository;
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
    private final DetallePedidoRepository detallePedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PedidoClienteResumenResponse> listar(String emailCliente) {
        Cliente cliente = obtenerCliente(emailCliente);

        return pedidoRepository
                .findByCliente_IdClienteAndTipoVentaOrderByFechaCreacionDesc(
                        cliente.getIdCliente(),
                        TipoVenta.ONLINE
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoClienteDetalleResponse buscarDetalle(
            String emailCliente,
            Integer idPedido
    ) {
        if (idPedido == null || idPedido <= 0) {
            throw pedidoNoDisponible();
        }

        Cliente cliente = obtenerCliente(emailCliente);

        Pedido pedido = pedidoRepository
                .findByIdPedidoAndCliente_IdClienteAndTipoVenta(
                        idPedido,
                        cliente.getIdCliente(),
                        TipoVenta.ONLINE
                )
                .orElseThrow(this::pedidoNoDisponible);

        List<PedidoClienteProductoResponse> productos =
                detallePedidoRepository
                        .findByPedido_IdPedidoOrderByIdDetallePedidoAsc(idPedido)
                        .stream()
                        .map(this::convertirProducto)
                        .toList();

        return new PedidoClienteDetalleResponse(
                pedido.getIdPedido(),
                pedido.getNumeroPedido(),
                pedido.getFechaCreacion(),
                pedido.getFechaActualizacion(),
                pedido.getEstadoPedido(),
                pedido.getEstadoPago(),
                pedido.getMedioPago().getNombre(),
                pedido.getMedioEntrega(),
                pedido.getLocalidadEnvio(),
                pedido.getDireccionEnvio(),
                productos,
                pedido.getSubtotal(),
                pedido.getDescuentoCliente(),
                pedido.getDescuentoMayorista(),
                pedido.getDescuentoOferta(),
                pedido.getTotal(),
                pedido.getObservacionesCliente()
        );
    }

    private Cliente obtenerCliente(String emailCliente) {
        if (emailCliente == null || emailCliente.isBlank()) {
            throw new PedidoNoEncontradoException(
                    "No existe un cliente asociado a la sesión"
            );
        }

        return clienteRepository
                .findByUsuario_EmailIgnoreCase(emailCliente)
                .orElseThrow(() -> new PedidoNoEncontradoException(
                        "No existe un cliente asociado a la sesión"
                ));
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
                pedido.getLocalidadEnvio(),
                pedido.getDireccionEnvio(),
                pedido.getTotal()
        );
    }

    private PedidoClienteProductoResponse convertirProducto(
            DetallePedido detalle
    ) {
        return new PedidoClienteProductoResponse(
                detalle.getProducto().getIdProducto(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getDescuentoAplicado(),
                detalle.getSubtotal()
        );
    }

    private PedidoNoEncontradoException pedidoNoDisponible() {
        return new PedidoNoEncontradoException(
                "El pedido indicado no existe o no pertenece a tu cuenta"
        );
    }
}
