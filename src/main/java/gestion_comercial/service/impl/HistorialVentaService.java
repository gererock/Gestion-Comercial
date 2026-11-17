package gestion_comercial.service.impl;

import gestion_comercial.dto.response.HistorialVentaDetalleResponse;
import gestion_comercial.dto.response.HistorialVentaProductoResponse;
import gestion_comercial.dto.response.HistorialVentaResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.DetallePedido;
import gestion_comercial.entity.MedioPago;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.TipoVenta;
import gestion_comercial.repository.ClienteRepository;
import gestion_comercial.exception.PedidoNoEncontradoException;
import gestion_comercial.repository.DetallePedidoRepository;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.service.interfaces.IHistorialVentaService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
public class HistorialVentaService
        implements IHistorialVentaService {

    private final PedidoRepository pedidoRepository;

    private final DetallePedidoRepository detallePedidoRepository;

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HistorialVentaResponse> listarHistorial(LocalDate fecha, LocalDate desde, LocalDate hasta,
            String cliente, BigDecimal montoMinimo, BigDecimal montoMaximo, TipoVenta tipoVenta) {
        validarFiltros(fecha, desde, hasta, cliente, montoMinimo, montoMaximo);
        var specification = construirFiltros(fecha, desde, hasta, obtenerIdsClientes(cliente),
                montoMinimo, montoMaximo, tipoVenta);

        return pedidoRepository
                .findAll(specification, Sort.by(Sort.Order.desc("fechaCreacion"), Sort.Order.desc("idPedido")))
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

    private void validarFiltros(LocalDate fecha, LocalDate desde, LocalDate hasta,
            String cliente, BigDecimal montoMinimo, BigDecimal montoMaximo) {
        if (fecha != null && (desde != null || hasta != null)) {
            throw new IllegalArgumentException("No se puede usar una fecha exacta junto con un rango de fechas");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta");
        }
        if (montoMinimo != null && montoMinimo.signum() < 0) {
            throw new IllegalArgumentException("El monto mínimo no puede ser negativo");
        }
        if (montoMaximo != null && montoMaximo.signum() < 0) {
            throw new IllegalArgumentException("El monto máximo no puede ser negativo");
        }
        if (montoMinimo != null && montoMaximo != null && montoMinimo.compareTo(montoMaximo) > 0) {
            throw new IllegalArgumentException("El monto mínimo no puede ser mayor al monto máximo");
        }
        if (cliente != null && cliente.strip().isEmpty()) {
            throw new IllegalArgumentException("El filtro de cliente no puede contener solo espacios");
        }
        if (cliente != null && cliente.codePoints().noneMatch(Character::isLetterOrDigit)) {
            throw new IllegalArgumentException("El filtro de cliente contiene un valor inválido");
        }
    }

    private List<Integer> obtenerIdsClientes(String cliente) {
        if (cliente == null) {
            return null;
        }
        var clientes = clienteRepository.buscarClientes(cliente.strip());
        if (clientes.isEmpty()) {
            throw new IllegalArgumentException("No existe ningún cliente que coincida con la búsqueda");
        }
        return clientes.stream().map(Cliente::getIdCliente).toList();
    }

    private Specification<Pedido> construirFiltros(LocalDate fecha, LocalDate desde, LocalDate hasta,
            List<Integer> clientes, BigDecimal montoMinimo, BigDecimal montoMaximo, TipoVenta tipoVenta) {
        return (root, query, cb) -> {
            var filtros = new ArrayList<Predicate>();
            LocalDate inicio = fecha != null ? fecha : desde;
            LocalDate fin = fecha != null ? fecha : hasta;
            if (inicio != null) {
                filtros.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), inicio.atStartOfDay()));
            }
            if (fin != null) {
                // LocalDate.MAX no admite plusDays(1); todo LocalDateTime válido queda incluido.
                if (!fin.equals(LocalDate.MAX)) {
                    filtros.add(cb.lessThan(root.get("fechaCreacion"), fin.plusDays(1).atStartOfDay()));
                }
            }
            if (clientes != null) {
                filtros.add(root.get("cliente").get("idCliente").in(clientes));
            }
            if (montoMinimo != null) {
                filtros.add(cb.greaterThanOrEqualTo(root.get("total"), montoMinimo));
            }
            if (montoMaximo != null) {
                filtros.add(cb.lessThanOrEqualTo(root.get("total"), montoMaximo));
            }
            if (tipoVenta != null) {
                filtros.add(cb.equal(root.get("tipoVenta"), tipoVenta));
            }
            return cb.and(filtros.toArray(Predicate[]::new));
        };
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