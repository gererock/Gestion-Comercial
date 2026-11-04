package gestion_comercial.service.impl;

import gestion_comercial.dto.response.HistorialVentaDetalleResponse;
import gestion_comercial.dto.response.HistorialVentaProductoResponse;
import gestion_comercial.dto.response.HistorialVentaResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.DetallePedido;
import gestion_comercial.entity.MedioPago;
import gestion_comercial.entity.Pedido;
import gestion_comercial.exception.PedidoNoEncontradoException;
import gestion_comercial.repository.DetallePedidoRepository;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.service.interfaces.IHistorialVentaService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialVentaService
        implements IHistorialVentaService {

    private final PedidoRepository pedidoRepository;

    private final DetallePedidoRepository detallePedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HistorialVentaResponse> listarHistorial() {

        return pedidoRepository
                .findAllByOrderByFechaCreacionDescIdPedidoDesc()
                .stream()
                .map(this::convertirVenta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialVentaDetalleResponse buscarDetalle(
            Integer idPedido) {

        validarIdPedido(
                idPedido);

        Pedido pedido = pedidoRepository
                .findByIdPedido(
                        idPedido)
                .orElseThrow(
                        () -> new PedidoNoEncontradoException(
                                "La venta indicada no existe"));

        List<HistorialVentaProductoResponse> productos = detallePedidoRepository
                .findByPedido_IdPedidoOrderByIdDetallePedidoAsc(
                        idPedido)
                .stream()
                .map(this::convertirProducto)
                .toList();

        return convertirDetalle(
                pedido,
                productos);
    }

    private HistorialVentaDetalleResponse convertirDetalle(
            Pedido pedido,
            List<HistorialVentaProductoResponse> productos) {

        String cliente = obtenerNombreCliente(
                pedido.getCliente());

        String medioPago = obtenerMedioPago(
                pedido.getMedioPago());

        return new HistorialVentaDetalleResponse(

                pedido.getIdPedido(),

                pedido.getNumeroPedido(),

                pedido.getFechaCreacion(),

                cliente,

                pedido.getTipoVenta(),

                pedido.getEstadoPedido(),

                pedido.getEstadoPago(),

                medioPago,

                pedido.getMedioEntrega(),

                productos,

                pedido.getSubtotal(),

                pedido.getDescuentoCliente(),

                pedido.getDescuentoMayorista(),

                pedido.getDescuentoOferta(),

                pedido.getTotal());
    }

    private HistorialVentaProductoResponse convertirProducto(DetallePedido detalle) {
        return new HistorialVentaProductoResponse(

                detalle.getProducto()
                        .getIdProducto(),

                detalle.getProducto()
                        .getNombre(),

                detalle.getCantidad(),

                detalle.getPrecioUnitario(),

                detalle.getDescuentoAplicado(),

                detalle.getSubtotal());
    }

    private void validarIdPedido(Integer idPedido) {
        if (idPedido == null || idPedido <= 0) {
            throw new PedidoNoEncontradoException("La venta indicada no existe");
        }
    }

    private HistorialVentaResponse convertirVenta(
            Pedido pedido) {

        String cliente = obtenerNombreCliente(
                pedido.getCliente());

        String medioPago = obtenerMedioPago(
                pedido.getMedioPago());

        return new HistorialVentaResponse(

                pedido.getIdPedido(),

                pedido.getNumeroPedido(),

                pedido.getFechaCreacion(),

                cliente,

                pedido.getTipoVenta(),

                pedido.getEstadoPedido(),

                pedido.getEstadoPago(),

                medioPago,

                pedido.getMedioEntrega(),

                pedido.getTotal());
    }

    private String obtenerNombreCliente(
            Cliente cliente) {

        if (cliente == null) {

            return "Consumidor final";
        }

        if (cliente.getRazonSocial() != null
                &&
                !cliente.getRazonSocial().isBlank()) {

            return cliente.getRazonSocial();
        }

        String nombre = cliente.getNombre() != null
                ? cliente.getNombre()
                : "";

        String apellido = cliente.getApellido() != null
                ? cliente.getApellido()
                : "";

        String nombreCompleto = (nombre + " " + apellido)
                .trim();

        if (nombreCompleto.isBlank()) {

            return "Cliente";
        }

        return nombreCompleto;
    }

    private String obtenerMedioPago(
            MedioPago medioPago) {

        if (medioPago == null) {

            return "Sin medio de pago";
        }

        return medioPago.getNombre();
    }
}