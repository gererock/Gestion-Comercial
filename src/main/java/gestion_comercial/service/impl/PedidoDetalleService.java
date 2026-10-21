package gestion_comercial.service.impl;

import gestion_comercial.dto.response.PedidoDetalleClienteResponse;
import gestion_comercial.dto.response.PedidoDetalleProductoResponse;
import gestion_comercial.dto.response.PedidoDetalleResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.DetallePedido;
import gestion_comercial.entity.MedioEntrega;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.Producto;
import gestion_comercial.entity.TipoVenta;
import gestion_comercial.exception.PedidoNoEncontradoException;
import gestion_comercial.repository.DetallePedidoRepository;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.service.interfaces.IPedidoDetalleService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoDetalleService implements IPedidoDetalleService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public PedidoDetalleResponse buscarDetalle(Integer idPedido) {
        validarId(idPedido);

        Pedido pedido = pedidoRepository
                .findByIdPedidoAndTipoVentaAndClienteIsNotNull(
                        idPedido,
                        TipoVenta.ONLINE
                )
                .orElseThrow(() -> new PedidoNoEncontradoException(
                        "El pedido online indicado no existe"
                ));

        validarEntrega(pedido);

        List<PedidoDetalleProductoResponse> productos =
                detallePedidoRepository
                        .findByPedido_IdPedidoOrderByIdDetallePedidoAsc(idPedido)
                        .stream()
                        .map(this::convertirProducto)
                        .toList();

        return convertirPedido(pedido, productos);
    }

    private void validarId(Integer idPedido) {
        if (idPedido == null || idPedido <= 0) {
            throw new PedidoNoEncontradoException(
                    "El pedido online indicado no existe"
            );
        }
    }

    private void validarEntrega(Pedido pedido) {
        if (pedido.getMedioEntrega() == MedioEntrega.ENVIO
                && (pedido.getLocalidadEnvio() == null
                || pedido.getDireccionEnvio() == null
                || pedido.getDireccionEnvio().isBlank())) {
            throw new PedidoNoEncontradoException(
                    "El pedido online posee datos de envío incompletos"
            );
        }
    }

    private PedidoDetalleResponse convertirPedido(
            Pedido pedido,
            List<PedidoDetalleProductoResponse> productos
    ) {
        Cliente cliente = pedido.getCliente();

        PedidoDetalleClienteResponse clienteResponse =
                new PedidoDetalleClienteResponse(
                        cliente.getIdCliente(),
                        cliente.getNombre(),
                        cliente.getApellido(),
                        cliente.getRazonSocial(),
                        cliente.getDni(),
                        cliente.getCuit(),
                        cliente.getTelefono(),
                        cliente.getCorreo()
                );

        return new PedidoDetalleResponse(
                pedido.getIdPedido(),
                pedido.getNumeroPedido(),
                pedido.getFechaCreacion(),
                pedido.getFechaActualizacion(),
                pedido.getEstadoPedido(),
                pedido.getEstadoPago(),
                pedido.getMedioPago().getIdMedioPago(),
                pedido.getMedioPago().getNombre(),
                pedido.getMedioEntrega(),
                pedido.getLocalidadEnvio(),
                pedido.getDireccionEnvio(),
                clienteResponse,
                productos,
                pedido.getSubtotal(),
                pedido.getDescuentoCliente(),
                pedido.getDescuentoMayorista(),
                pedido.getDescuentoOferta(),
                pedido.getTotal(),
                pedido.getObservacionesCliente(),
                pedido.getObservacionesInternas()
        );
    }

    private PedidoDetalleProductoResponse convertirProducto(
            DetallePedido detalle
    ) {
        Producto producto = detalle.getProducto();
        int stockDisponible = producto.getStockActual() == null
                ? 0
                : producto.getStockActual();
        int cantidad = detalle.getCantidad() == null
                ? 0
                : detalle.getCantidad();

        return new PedidoDetalleProductoResponse(
                producto.getIdProducto(),
                producto.getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getDescuentoAplicado(),
                detalle.getSubtotal(),
                stockDisponible,
                stockDisponible >= cantidad
        );
    }
}
