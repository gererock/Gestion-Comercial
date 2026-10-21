package gestion_comercial.service.impl;

import gestion_comercial.dto.response.PedidoDecisionResponse;
import gestion_comercial.entity.DetallePedido;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.EstadoProducto;
import gestion_comercial.entity.EstadoReservaStock;
import gestion_comercial.entity.MovimientoStock;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.Producto;
import gestion_comercial.entity.ReservaStock;
import gestion_comercial.entity.TipoVenta;
import gestion_comercial.entity.Usuario;
import gestion_comercial.exception.PedidoDecisionInvalidaException;
import gestion_comercial.exception.PedidoNoEncontradoException;
import gestion_comercial.repository.DetallePedidoRepository;
import gestion_comercial.repository.MovimientoStockRepository;
import gestion_comercial.repository.PedidoRepository;
import gestion_comercial.repository.ProductoRepository;
import gestion_comercial.repository.ReservaStockRepository;
import gestion_comercial.repository.UsuarioRepository;
import gestion_comercial.service.interfaces.IPedidoDecisionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoDecisionService implements IPedidoDecisionService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;
    private final ReservaStockRepository reservaStockRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public PedidoDecisionResponse aprobar(
            Integer idPedido,
            String emailUsuario
    ) {
        Pedido pedido = obtenerPedidoPendiente(idPedido);
        Usuario usuario = obtenerUsuario(emailUsuario);
        List<LineaDecision> lineas = prepararLineas(pedido);

        if (lineas.isEmpty()) {
            throw new PedidoDecisionInvalidaException(
                    "No se puede aprobar un pedido sin productos"
            );
        }

        for (LineaDecision linea : lineas) {
            validarProductoActivo(linea.producto());

            if (!tieneReservaActiva(linea.reserva())) {
                validarStock(linea.detalle(), linea.producto());
            }
        }

        LocalDateTime ahora = LocalDateTime.now();

        for (LineaDecision linea : lineas) {
            if (!tieneReservaActiva(linea.reserva())) {
                reservarStock(linea, usuario, ahora);
            }
        }

        pedido.setEstadoPedido(EstadoPedido.CONFIRMADO);
        pedido.setFechaActualizacion(ahora);
        pedidoRepository.save(pedido);

        return respuesta(
                pedido,
                "El pedido fue aprobado correctamente"
        );
    }

    @Override
    @Transactional
    public PedidoDecisionResponse cancelar(
            Integer idPedido,
            String emailUsuario
    ) {
        Pedido pedido = obtenerPedidoPendiente(idPedido);
        Usuario usuario = obtenerUsuario(emailUsuario);
        List<LineaDecision> lineas = prepararLineas(pedido);
        LocalDateTime ahora = LocalDateTime.now();

        for (LineaDecision linea : lineas) {
            if (tieneReservaActiva(linea.reserva())) {
                liberarReserva(linea, usuario, ahora);
            }
        }

        pedido.setEstadoPedido(EstadoPedido.CANCELADO);
        pedido.setFechaActualizacion(ahora);
        pedidoRepository.save(pedido);

        return respuesta(
                pedido,
                "El pedido fue cancelado correctamente"
        );
    }

    private Pedido obtenerPedidoPendiente(Integer idPedido) {
        if (idPedido == null || idPedido <= 0) {
            throw new PedidoNoEncontradoException(
                    "El pedido indicado no existe"
            );
        }

        Pedido pedido = pedidoRepository
                .buscarPorIdParaDecision(idPedido)
                .orElseThrow(() -> new PedidoNoEncontradoException(
                        "El pedido indicado no existe"
                ));

        if (pedido.getTipoVenta() != TipoVenta.ONLINE) {
            throw new PedidoDecisionInvalidaException(
                    "Solo se pueden aprobar o cancelar pedidos online"
            );
        }

        if (pedido.getEstadoPedido() != EstadoPedido.PENDIENTE_APROBACION) {
            throw new PedidoDecisionInvalidaException(
                    "El pedido ya no está pendiente de aprobación"
            );
        }

        return pedido;
    }

    private Usuario obtenerUsuario(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new PedidoDecisionInvalidaException(
                    "No se pudo identificar al usuario que realiza la acción"
            );
        }

        return usuarioRepository
                .findByEmailIgnoreCase(emailUsuario)
                .orElseThrow(() -> new PedidoDecisionInvalidaException(
                        "No se pudo identificar al usuario que realiza la acción"
                ));
    }

    private List<LineaDecision> prepararLineas(Pedido pedido) {
        List<DetallePedido> detalles = detallePedidoRepository
                .findByPedido_IdPedidoOrderByIdDetallePedidoAsc(
                        pedido.getIdPedido()
                );
        List<LineaDecision> lineas = new ArrayList<>();

        for (DetallePedido detalle : detalles) {
            Producto producto = productoRepository
                    .buscarParaReservar(
                            detalle.getProducto().getIdProducto()
                    )
                    .orElseThrow(() -> new PedidoDecisionInvalidaException(
                            "Uno de los productos del pedido ya no existe"
                    ));

            ReservaStock reserva = reservaStockRepository
                    .buscarPorDetalleParaDecision(
                            detalle.getIdDetallePedido()
                    )
                    .orElse(null);

            lineas.add(
                    new LineaDecision(detalle, producto, reserva)
            );
        }

        return lineas;
    }

    private void validarProductoActivo(Producto producto) {
        if (producto.getEstado() != EstadoProducto.ACTIVO) {
            throw new PedidoDecisionInvalidaException(
                    "El producto '" + producto.getNombre()
                            + "' está pausado o discontinuado"
            );
        }
    }

    private void validarStock(
            DetallePedido detalle,
            Producto producto
    ) {
        int cantidad = cantidadValida(detalle);
        int stock = producto.getStockActual() == null
                ? 0
                : producto.getStockActual();

        if (stock < cantidad) {
            throw new PedidoDecisionInvalidaException(
                    "No hay stock suficiente para '"
                            + producto.getNombre() + "'"
            );
        }
    }

    private void reservarStock(
            LineaDecision linea,
            Usuario usuario,
            LocalDateTime ahora
    ) {
        int cantidad = cantidadValida(linea.detalle());
        Producto producto = linea.producto();
        int stockAnterior = producto.getStockActual();
        int stockPosterior = stockAnterior - cantidad;

        producto.setStockActual(stockPosterior);
        productoRepository.save(producto);

        ReservaStock reserva = linea.reserva();
        if (reserva == null) {
            reserva = new ReservaStock();
            reserva.setDetallePedido(linea.detalle());
        }

        reserva.setCantidad(cantidad);
        reserva.setEstado(EstadoReservaStock.ACTIVA);
        reserva.setFechaReserva(ahora);
        reserva.setFechaCierre(null);
        reservaStockRepository.save(reserva);

        registrarMovimiento(
                producto,
                usuario,
                "RESERVA",
                cantidad,
                stockAnterior,
                stockPosterior,
                ahora
        );
    }

    private void liberarReserva(
            LineaDecision linea,
            Usuario usuario,
            LocalDateTime ahora
    ) {
        ReservaStock reserva = linea.reserva();
        Producto producto = linea.producto();
        int cantidad = reserva.getCantidad() == null
                ? cantidadValida(linea.detalle())
                : reserva.getCantidad();
        int stockAnterior = producto.getStockActual() == null
                ? 0
                : producto.getStockActual();
        int stockPosterior = stockAnterior + cantidad;

        producto.setStockActual(stockPosterior);
        productoRepository.save(producto);

        reserva.setEstado(EstadoReservaStock.LIBERADA);
        reserva.setFechaCierre(ahora);
        reservaStockRepository.save(reserva);

        registrarMovimiento(
                producto,
                usuario,
                "LIBERACION_RESERVA",
                cantidad,
                stockAnterior,
                stockPosterior,
                ahora
        );
    }

    private int cantidadValida(DetallePedido detalle) {
        if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
            throw new PedidoDecisionInvalidaException(
                    "El pedido contiene una cantidad de producto inválida"
            );
        }

        return detalle.getCantidad();
    }

    private boolean tieneReservaActiva(ReservaStock reserva) {
        return reserva != null
                && reserva.getEstado() == EstadoReservaStock.ACTIVA;
    }

    private void registrarMovimiento(
            Producto producto,
            Usuario usuario,
            String tipo,
            int cantidad,
            int stockAnterior,
            int stockPosterior,
            LocalDateTime ahora
    ) {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProducto(producto);
        movimiento.setUsuario(usuario);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setOrigen("RESERVA_PEDIDO");
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockPosterior(stockPosterior);
        movimiento.setFecha(ahora);
        movimientoStockRepository.save(movimiento);
    }

    private PedidoDecisionResponse respuesta(
            Pedido pedido,
            String mensaje
    ) {
        return new PedidoDecisionResponse(
                pedido.getIdPedido(),
                pedido.getNumeroPedido(),
                pedido.getEstadoPedido(),
                pedido.getFechaActualizacion(),
                mensaje
        );
    }

    private record LineaDecision(
            DetallePedido detalle,
            Producto producto,
            ReservaStock reserva
    ) {
    }
}
