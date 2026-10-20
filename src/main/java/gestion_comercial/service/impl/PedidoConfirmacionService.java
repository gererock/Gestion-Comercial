
package gestion_comercial.service.impl;

import gestion_comercial.dto.request.*;
import gestion_comercial.dto.response.*;

import gestion_comercial.entity.*;
import gestion_comercial.exception.PedidoConfirmacionException;
import gestion_comercial.repository.*;
import gestion_comercial.service.interfaces.IConfiguracionComercioService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PedidoConfirmacionService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final DireccionEnvioRepository direccionRepository;
    private final DetallePedidoRepository detalleRepository;
    private final ReservaStockRepository reservaRepository;
    private final MovimientoStockRepository movimientoRepository;
    private final IConfiguracionComercioService configuracionService;

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal MAX_IMPORTE =
            new BigDecimal("9999999999.99");

    public PedidoConfirmacionService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            MedioPagoRepository medioPagoRepository,
            DireccionEnvioRepository direccionRepository,
            DetallePedidoRepository detalleRepository,
            ReservaStockRepository reservaRepository,
            MovimientoStockRepository movimientoRepository,
            IConfiguracionComercioService configuracionService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.direccionRepository = direccionRepository;
        this.detalleRepository = detalleRepository;
        this.reservaRepository = reservaRepository;
        this.movimientoRepository = movimientoRepository;
        this.configuracionService = configuracionService;
    }

    @Transactional
    public ConfirmarPedidoResponse confirmar(
            String email,
            ConfirmarPedidoRequest request
    ) {

        // Bloqueamos al cliente para serializar sus confirmaciones.
        Cliente cliente = clienteRepository
                .buscarParaConfirmar(email)
                .orElseThrow(() -> new PedidoConfirmacionException(
                        "No existe un cliente asociado a tu cuenta."
                ));

        if (!Boolean.TRUE.equals(cliente.getEstado())
                || cliente.getUsuario() == null
                || cliente.getUsuario().getEstado() != EstadoUsuario.ACTIVO) {
            throw new PedidoConfirmacionException(
                    "El cliente no está habilitado para comprar."
            );
        }

        // Reintentar la misma confirmación no debe crear otro pedido.
        Optional<Pedido> existente = pedidoRepository
                .findByClaveConfirmacion(request.claveConfirmacion());

        if (existente.isPresent()) {
            Pedido anterior = existente.get();

            if (anterior.getCliente() == null
                    || !anterior.getCliente().getIdCliente()
                            .equals(cliente.getIdCliente())) {
                throw new PedidoConfirmacionException(
                        "La confirmación indicada no es válida."
                );
            }

            return respuesta(anterior);
        }

        MedioPago medioPago = medioPagoRepository
                .findByIdMedioPagoAndActivoTrue(request.idMedioPago())
                .orElseThrow(() -> new PedidoConfirmacionException(
                        "El medio de pago no está disponible."
                ));

        // Validamos y ordenamos los productos para bloquearlos siempre
        // en el mismo orden y reducir posibles interbloqueos.
        TreeMap<Integer, Integer> cantidades = new TreeMap<>();

        if (request.productos() == null
                || request.productos().isEmpty()
                || request.productos().size() > 100) {
            throw new PedidoConfirmacionException(
                    "El carrito debe contener productos válidos."
            );
        }

        for (ItemConfirmarPedidoRequest item : request.productos()) {

            if (item == null
                    || item.idProducto() == null
                    || item.idProducto() <= 0
                    || item.cantidad() == null
                    || item.cantidad() <= 0) {
                throw new PedidoConfirmacionException(
                        "El carrito contiene productos o cantidades inválidas."
                );
            }

            if (cantidades.putIfAbsent(
                    item.idProducto(), item.cantidad()) != null) {
                throw new PedidoConfirmacionException(
                        "Hay productos repetidos en el carrito."
                );
            }
        }

        List<Linea> lineas = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descuentoOferta = BigDecimal.ZERO;
        BigDecimal descuentoMayorista = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entrada : cantidades.entrySet()) {

            Producto producto = productoRepository
                    .buscarParaReservar(entrada.getKey())
                    .orElseThrow(() -> new PedidoConfirmacionException(
                            "Uno de los productos ya no existe."
                    ));

            if (producto.getEstado() != EstadoProducto.ACTIVO
                    || !Boolean.TRUE.equals(producto.getPublicadoOnline())) {
                throw new PedidoConfirmacionException(
                        "El producto " + producto.getNombre()
                                + " ya no está disponible online."
                );
            }

            int cantidad = entrada.getValue();

            if (producto.getStockActual() == null
                    || producto.getStockActual() < cantidad) {
                throw new PedidoConfirmacionException(
                        "Stock insuficiente para " + producto.getNombre() + "."
                );
            }

            Linea linea = calcularLinea(producto, cantidad);
            lineas.add(linea);

            subtotal = subtotal.add(linea.bruto());
            descuentoOferta = descuentoOferta.add(linea.oferta());
            descuentoMayorista = descuentoMayorista.add(linea.mayorista());
        }

        subtotal = dinero(subtotal);
        descuentoOferta = dinero(descuentoOferta);
        descuentoMayorista = dinero(descuentoMayorista);

        BigDecimal netoProductos = subtotal
                .subtract(descuentoOferta)
                .subtract(descuentoMayorista);

        BigDecimal porcentajeCliente = cliente.getDescuentoPorcentaje();
        if (porcentajeCliente == null) {
            porcentajeCliente = BigDecimal.ZERO;
        }

        if (porcentajeCliente.compareTo(BigDecimal.ZERO) < 0
                || porcentajeCliente.compareTo(CIEN) > 0) {
            throw new PedidoConfirmacionException(
                    "El descuento del cliente no es válido."
            );
        }

        BigDecimal descuentoCliente = dinero(
                netoProductos.multiply(porcentajeCliente)
                        .divide(CIEN, 6, RoundingMode.HALF_UP)
        );

        BigDecimal total = dinero(netoProductos.subtract(descuentoCliente));

        validarImporte(subtotal);
        validarImporte(descuentoOferta);
        validarImporte(descuentoMayorista);
        validarImporte(descuentoCliente);
        validarImporte(total);

        // El mínimo se aplica al total final, incluido el descuento cliente.
        DireccionEnvioCliente direccion =
                validarEntrega(cliente, request, total);

        LocalDateTime ahora = LocalDateTime.now();

        Pedido pedido = new Pedido();

        // Número temporal único, reemplazado cuando PostgreSQL asigna ID.
        pedido.setNumeroPedido(
                "TMP-" + UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 20)
        );

        pedido.setClaveConfirmacion(request.claveConfirmacion());
        pedido.setCliente(cliente);
        pedido.setMedioPago(medioPago);
        pedido.setTipoVenta(TipoVenta.ONLINE);
        pedido.setEstadoPedido(EstadoPedido.PENDIENTE_APROBACION);
        pedido.setEstadoPago(EstadoPago.PENDIENTE);
        pedido.setMedioEntrega(request.medioEntrega());
        pedido.setFechaCreacion(ahora);
        pedido.setFechaActualizacion(ahora);
        pedido.setSubtotal(subtotal);
        pedido.setDescuentoOferta(descuentoOferta);
        pedido.setDescuentoMayorista(descuentoMayorista);
        pedido.setDescuentoCliente(descuentoCliente);
        pedido.setTotal(total);

        String observaciones = request.observacionesCliente();
        pedido.setObservacionesCliente(
                observaciones == null || observaciones.isBlank()
                        ? null
                        : observaciones.trim()
        );

        if (direccion != null) {

            pedido.setDireccionEnvioCliente(direccion);
            pedido.setLocalidadEnvio(direccion.getLocalidad());

            String domicilio = direccion.getCalle()
                    + " " + direccion.getNumero();

            if (direccion.getPisoDepartamento() != null
                    && !direccion.getPisoDepartamento().isBlank()) {
                domicilio += ", " + direccion.getPisoDepartamento();
            }

            if (domicilio.length() > 200) {
                throw new PedidoConfirmacionException(
                        "La dirección de envío es demasiado extensa."
                );
            }

            pedido.setDireccionEnvio(domicilio);

        } else {

            pedido.setDireccionEnvioCliente(null);
            pedido.setLocalidadEnvio(null);
            pedido.setDireccionEnvio(null);
        }

        pedidoRepository.saveAndFlush(pedido);
        pedido.setNumeroPedido("PED-" + pedido.getIdPedido());

        // Reservas y movimientos pertenecen a la misma transacción.
        for (Linea linea : lineas) {

            Producto producto = linea.producto();

            int stockAnterior = producto.getStockActual();
            int stockPosterior = stockAnterior - linea.cantidad();

            producto.setStockActual(stockPosterior);
            productoRepository.save(producto);

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(linea.cantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setCostoUnitario(producto.getPrecioCosto());
            detalle.setDescuentoAplicado(
                    dinero(linea.oferta().add(linea.mayorista()))
            );
            detalle.setSubtotal(linea.neto());

            detalleRepository.save(detalle);

            ReservaStock reserva = new ReservaStock();
            reserva.setDetallePedido(detalle);
            reserva.setCantidad(linea.cantidad());
            reserva.setEstado(EstadoReservaStock.ACTIVA);
            reservaRepository.save(reserva);

            MovimientoStock movimiento = new MovimientoStock();
            movimiento.setProducto(producto);
            movimiento.setUsuario(cliente.getUsuario());
            movimiento.setTipoMovimiento("RESERVA");
            movimiento.setOrigen("RESERVA_PEDIDO");
            movimiento.setCantidad(linea.cantidad());
            movimiento.setStockAnterior(stockAnterior);
            movimiento.setStockPosterior(stockPosterior);
            movimiento.setFecha(ahora);

            movimientoRepository.save(movimiento);
        }

        // Al hacer commit se guardará todo junto o nada.
        return respuesta(pedido);
    }

    private DireccionEnvioCliente validarEntrega(
            Cliente cliente,
            ConfirmarPedidoRequest request,
            BigDecimal total
    ) {

        if (request.medioEntrega() == MedioEntrega.RETIRO_LOCAL) {

            if (request.idDireccionEnvio() != null) {
                throw new PedidoConfirmacionException(
                        "Retiro en local no necesita dirección de envío."
                );
            }

            return null;
        }

        if (request.medioEntrega() != MedioEntrega.ENVIO) {
            throw new PedidoConfirmacionException(
                    "La modalidad de entrega no es válida."
            );
        }

        if (request.idDireccionEnvio() == null) {
            throw new PedidoConfirmacionException(
                    "Seleccioná una dirección de envío."
            );
        }

        ConfiguracionCheckoutEnvioResponse configuracion =
                configuracionService.obtenerCheckout();

        if (total.compareTo(configuracion.montoMinimoEnvio()) < 0) {
            throw new PedidoConfirmacionException(
                    "El pedido no alcanza el monto mínimo de envío."
            );
        }

        DireccionEnvioCliente direccion = direccionRepository
                .findByIdDireccionEnvioAndCliente_IdCliente(
                        request.idDireccionEnvio(),
                        cliente.getIdCliente()
                )
                .orElseThrow(() -> new PedidoConfirmacionException(
                        "La dirección no existe o no pertenece a tu cuenta."
                ));

        boolean habilitada = switch (direccion.getLocalidad()) {

            case VILLA_MARIA ->
                    Boolean.TRUE.equals(
                            configuracion.permiteEnvioVillaMaria()
                    );

            case VILLA_NUEVA ->
                    Boolean.TRUE.equals(
                            configuracion.permiteEnvioVillaNueva()
                    );
        };

        if (!habilitada) {
            throw new PedidoConfirmacionException(
                    "La localidad de envío no está habilitada."
            );
        }

        return direccion;
    }

    private Linea calcularLinea(Producto producto, int cantidad) {

        BigDecimal precio = producto.getPrecioVenta();

        if (precio == null
                || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PedidoConfirmacionException(
                    "El precio del producto no es válido."
            );
        }

        BigDecimal bruto = dinero(
                precio.multiply(BigDecimal.valueOf(cantidad))
        );

        BigDecimal oferta = BigDecimal.ZERO;
        BigDecimal mayorista = BigDecimal.ZERO;

        // La oferta tiene prioridad, igual que en el carrito.
        if (Boolean.TRUE.equals(producto.getEnOferta())) {

            oferta = descuento(bruto, producto.getPorcentajeDescuentoOferta());

        } else if (producto.getCantidadMinimaMayorista() != null
                && producto.getPorcentajeDescuentoMayorista() != null
                && cantidad >= producto.getCantidadMinimaMayorista()) {

            mayorista = descuento(
                    bruto,
                    producto.getPorcentajeDescuentoMayorista()
            );
        }

        BigDecimal neto = dinero(
                bruto.subtract(oferta).subtract(mayorista)
        );

        validarImporte(bruto);
        validarImporte(neto);

        return new Linea(
                producto, cantidad, bruto, oferta, mayorista, neto
        );
    }

    private BigDecimal descuento(BigDecimal monto, BigDecimal porcentaje) {

        if (porcentaje == null
                || porcentaje.compareTo(BigDecimal.ZERO) < 0
                || porcentaje.compareTo(CIEN) > 0) {
            throw new PedidoConfirmacionException(
                    "El descuento configurado no es válido."
            );
        }

        return dinero(
                monto.multiply(porcentaje)
                        .divide(CIEN, 6, RoundingMode.HALF_UP)
        );
    }

    private BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private void validarImporte(BigDecimal importe) {
        if (importe.compareTo(BigDecimal.ZERO) < 0
                || importe.compareTo(MAX_IMPORTE) > 0) {
            throw new PedidoConfirmacionException(
                    "El importe del pedido supera los límites permitidos."
            );
        }
    }

    private ConfirmarPedidoResponse respuesta(Pedido pedido) {

        return new ConfirmarPedidoResponse(
                pedido.getIdPedido(),
                pedido.getNumeroPedido(),
                pedido.getEstadoPedido(),
                pedido.getEstadoPago(),
                pedido.getMedioEntrega(),
                pedido.getLocalidadEnvio(),
                pedido.getSubtotal(),
                pedido.getDescuentoOferta(),
                pedido.getDescuentoMayorista(),
                pedido.getDescuentoCliente(),
                pedido.getTotal(),
                pedido.getFechaCreacion()
        );
    }

    private record Linea(
            Producto producto,
            int cantidad,
            BigDecimal bruto,
            BigDecimal oferta,
            BigDecimal mayorista,
            BigDecimal neto
    ) {
    }
}
