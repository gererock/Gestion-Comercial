package gestion_comercial.service.impl;

import gestion_comercial.dto.request.VentaLocalRequest;
import gestion_comercial.dto.response.ProductoVentaLocalResponse;
import gestion_comercial.dto.response.VentaLocalResponse;
import gestion_comercial.entity.*;
import gestion_comercial.exception.*;
import gestion_comercial.repository.*;
import gestion_comercial.service.impl.PrecioPedidoCalculator.Linea;
import gestion_comercial.service.interfaces.IVentaLocalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static gestion_comercial.service.impl.PrecioPedidoCalculator.*;

@Service
@RequiredArgsConstructor
public class VentaLocalService implements IVentaLocalService {

    private final PedidoRepository pedidos;
    private final ProductoRepository productos;
    private final DetallePedidoRepository detalles;
    private final MovimientoStockRepository movimientos;
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final MedioPagoRepository medios;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoVentaLocalResponse> buscar(Integer idProducto, String nombre) {
        if (idProducto != null && idProducto <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
        return productos.consultar(idProducto, nombre == null ? "" : nombre.trim(),
                        null, null, EstadoProducto.ACTIVO, false)
                .stream()
                .filter(p -> p.getStockActual() > 0)
                .map(ProductoVentaLocalResponse::de)
                .toList();
    }

    @Override
    @Transactional
    public VentaLocalResponse confirmar(String email, VentaLocalRequest request) {
        TreeMap<Integer, Integer> cantidades = consolidar(request);
        String descripcion = normalizarPago(request);

        // Serializa reintentos del mismo responsable antes de consultar la clave única.
        // La restricción UNIQUE de clave_confirmacion protege también entre responsables.
        Usuario usuario = usuarios.buscarParaVentaLocal(email)
                .orElseThrow(() -> new UsuarioNoHabilitadoException("Usuario no habilitado."));
        if (usuario.getEstado() != EstadoUsuario.ACTIVO || usuario.getRol() == null
                || !Set.of("VENDEDOR", "ADMINISTRADOR").contains(usuario.getRol().getNombre())) {
            throw new UsuarioNoHabilitadoException("Usuario no habilitado para registrar ventas.");
        }

        Optional<Pedido> anterior = pedidos.findByClaveConfirmacion(request.claveOperacion());
        if (anterior.isPresent()) {
            return recuperarOperacion(anterior.get(), usuario, cantidades, request, descripcion);
        }

        Cliente cliente = obtenerCliente(request.idCliente());
        MedioPago medio = medios.findByIdMedioPagoAndActivoTrue(request.idMedioPago())
                .orElseThrow(() -> new IllegalArgumentException("El medio de pago seleccionado no existe o no está activo."));
        List<Linea> lineas = new ArrayList<>();
        // TreeMap garantiza el mismo orden de bloqueo para todas las confirmaciones.
        for (Map.Entry<Integer, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productos.buscarParaReservar(entrada.getKey())
                    .orElseThrow(() -> new ProductoNoEncontradoException(
                            "No existe el producto " + entrada.getKey()));
            if (producto.getEstado() != EstadoProducto.ACTIVO) {
                throw new PedidoConfirmacionException(
                        "El producto " + producto.getNombre() + " no está activo.");
            }
            if (producto.getStockActual() == null || producto.getStockActual() < entrada.getValue()) {
                throw new PedidoConfirmacionException("Stock insuficiente para " + producto.getNombre());
            }
            lineas.add(calcularLinea(producto, entrada.getValue()));
        }

        BigDecimal bruto = lineas.stream().map(Linea::bruto)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        BigDecimal oferta = lineas.stream().map(Linea::oferta)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        BigDecimal mayorista = lineas.stream().map(Linea::mayorista)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        BigDecimal netoProductos = bruto.subtract(oferta).subtract(mayorista);
        BigDecimal descuentoCliente = calcularDescuentoCliente(netoProductos,
                cliente == null ? null : cliente.getDescuentoPorcentaje());
        BigDecimal total = netoProductos.subtract(descuentoCliente);
        validarImporte(bruto);
        validarImporte(total);

        LocalDateTime ahora = LocalDateTime.now();
        Pedido pedido = new Pedido();
        // Mismo patrón de numeración que la confirmación online.
        pedido.setNumeroPedido("TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        pedido.setClaveConfirmacion(request.claveOperacion());
        pedido.setVendedorResponsable(usuario);
        pedido.setCliente(cliente);
        pedido.setDescuentoCliente(descuentoCliente);
        pedido.setTipoVenta(TipoVenta.LOCAL);
        pedido.setEstadoPedido(EstadoPedido.ENTREGADO);
        pedido.setEstadoPago(request.estadoPago());
        pedido.setMedioPago(medio);
        pedido.setDescripcionPago(descripcion);
        pedido.setMedioEntrega(MedioEntrega.RETIRO_LOCAL);
        pedido.setFechaCreacion(ahora);
        pedido.setFechaActualizacion(ahora);
        pedido.setSubtotal(bruto);
        pedido.setDescuentoOferta(oferta);
        pedido.setDescuentoMayorista(mayorista);
        pedido.setTotal(total);

        pedidos.saveAndFlush(pedido);
        pedido.setNumeroPedido("PED-" + pedido.getIdPedido());

        List<DetallePedido> guardados = new ArrayList<>();
        for (Linea linea : lineas) {
            Producto producto = linea.producto();
            int stockAnterior = producto.getStockActual();
            producto.setStockActual(stockAnterior - linea.cantidad());

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(linea.cantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setCostoUnitario(producto.getPrecioCosto());
            detalle.setDescuentoAplicado(linea.oferta().add(linea.mayorista()).setScale(2));
            detalle.setSubtotal(linea.neto());
            detalles.save(detalle);
            guardados.add(detalle);

            MovimientoStock movimiento = new MovimientoStock();
            movimiento.setProducto(producto);
            movimiento.setUsuario(usuario);
            movimiento.setTipoMovimiento("SALIDA");
            movimiento.setOrigen("VENTA_LOCAL");
            movimiento.setCantidad(linea.cantidad());
            movimiento.setStockAnterior(stockAnterior);
            movimiento.setStockPosterior(producto.getStockActual());
            movimiento.setFecha(ahora);
            movimientos.save(movimiento);
        }
        // Los productos están administrados por JPA: dirty checking persiste el stock.
        pedidos.flush();
        return respuesta(pedido, guardados);
    }

    private String normalizarPago(VentaLocalRequest request) {
        if (request.idMedioPago() == null || request.idMedioPago() <= 0 || request.estadoPago() == null) {
            throw new IllegalArgumentException("Seleccioná un medio de pago y un estado válidos.");
        }
        String descripcion = request.descripcionPago() == null ? null : request.descripcionPago().trim();
        if (descripcion != null && descripcion.isEmpty()) descripcion = null;
        if (request.estadoPago() == EstadoPago.PENDIENTE
                && (descripcion == null || !descripcion.codePoints().anyMatch(Character::isLetterOrDigit))) {
            throw new IllegalArgumentException("El pago pendiente requiere una descripción con letras o números.");
        }
        return descripcion;
    }

    private Cliente obtenerCliente(Integer idCliente) {
        if (idCliente == null) return null;
        // Reutiliza el bloqueo de cliente utilizado para leer/modificar su descuento.
        Cliente cliente = clientes.buscarPorIdParaModificarDescuento(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException("El cliente indicado no existe."));
        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new PedidoConfirmacionException("El cliente indicado está inactivo.");
        }
        return cliente;
    }

    private TreeMap<Integer, Integer> consolidar(VentaLocalRequest request) {
        if (request == null || request.claveOperacion() == null || request.items() == null
                || request.items().isEmpty() || request.items().size() > 100) {
            throw new IllegalArgumentException(
                    "La venta debe contener entre 1 y 100 items y una clave de operación.");
        }
        if (request.idCliente() != null && request.idCliente() <= 0) {
            throw new IllegalArgumentException("El ID del cliente debe ser un entero positivo.");
        }
        TreeMap<Integer, Integer> cantidades = new TreeMap<>();
        for (VentaLocalRequest.Item item : request.items()) {
            if (item == null || item.idProducto() == null || item.idProducto() <= 0
                    || item.cantidad() == null || item.cantidad() <= 0) {
                throw new IllegalArgumentException("Productos y cantidades deben ser enteros positivos.");
            }
            long suma = (long) cantidades.getOrDefault(item.idProducto(), 0) + item.cantidad();
            if (suma > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Cantidad fuera de rango.");
            }
            cantidades.put(item.idProducto(), (int) suma);
        }
        return cantidades;
    }

    private VentaLocalResponse recuperarOperacion(Pedido pedido, Usuario usuario,
                                                  TreeMap<Integer, Integer> cantidades, VentaLocalRequest request, String descripcion) {
        if (pedido.getTipoVenta() != TipoVenta.LOCAL || pedido.getVendedorResponsable() == null
                || !usuario.getIdUsuario().equals(pedido.getVendedorResponsable().getIdUsuario())) {
            throw new PedidoConfirmacionException("La clave de operación pertenece a otra operación.");
        }
        Integer clienteOriginal = pedido.getCliente() == null ? null : pedido.getCliente().getIdCliente();
        if (!Objects.equals(clienteOriginal, request.idCliente())) {
            throw new PedidoConfirmacionException("La clave ya fue utilizada con otro cliente.");
        }
        if (pedido.getMedioPago() == null
                || !Objects.equals(pedido.getMedioPago().getIdMedioPago(), request.idMedioPago())
                || pedido.getEstadoPago() != request.estadoPago()
                || !Objects.equals(pedido.getDescripcionPago(), descripcion)) {
            throw new PedidoConfirmacionException("La clave ya fue utilizada con otros datos de pago.");
        }
        List<DetallePedido> guardados = detalles.findByPedido_IdPedidoOrderByIdDetallePedidoAsc(pedido.getIdPedido());
        TreeMap<Integer, Integer> previas = new TreeMap<>();
        guardados.forEach(d -> previas.put(d.getProducto().getIdProducto(), d.getCantidad()));
        if (!previas.equals(cantidades)) {
            throw new PedidoConfirmacionException("La clave ya fue utilizada con otros productos o cantidades.");
        }
        return respuesta(pedido, guardados);
    }

    private VentaLocalResponse respuesta(Pedido pedido, List<DetallePedido> guardados) {
        return new VentaLocalResponse(pedido.getIdPedido(), pedido.getNumeroPedido(),
                pedido.getEstadoPedido(), pedido.getEstadoPago(), pedido.getSubtotal(),
                pedido.getDescuentoOferta(), pedido.getDescuentoMayorista(), pedido.getTotal(),
                guardados.stream().map(d -> new VentaLocalResponse.Item(
                        d.getProducto().getIdProducto(), d.getProducto().getNombre(), d.getCantidad(),
                        d.getPrecioUnitario(), d.getDescuentoAplicado(), d.getSubtotal())).toList(),
                pedido.getDescuentoCliente(), pedido.getCliente() == null ? null :
                new VentaLocalResponse.ClienteVenta(pedido.getCliente().getIdCliente(),
                        pedido.getCliente().getNombre(), pedido.getCliente().getApellido()),
                pedido.getMedioPago() == null ? null : new VentaLocalResponse.MedioPagoVenta(
                        pedido.getMedioPago().getIdMedioPago(), pedido.getMedioPago().getNombre()),
                pedido.getDescripcionPago());
    }
}
