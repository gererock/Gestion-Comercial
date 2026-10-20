package gestion_comercial.service.impl;

import gestion_comercial.dto.response.PedidoDetalleResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.DetallePedido;
import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.LocalidadEnvio;
import gestion_comercial.entity.MedioEntrega;
import gestion_comercial.entity.MedioPago;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.Producto;
import gestion_comercial.entity.TipoVenta;
import gestion_comercial.exception.PedidoNoEncontradoException;
import gestion_comercial.repository.DetallePedidoRepository;
import gestion_comercial.repository.PedidoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoDetalleServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    private PedidoDetalleService pedidoDetalleService;

    @BeforeEach
    void setUp() {
        pedidoDetalleService = new PedidoDetalleService(
                pedidoRepository,
                detallePedidoRepository
        );
    }

    @ParameterizedTest
    @EnumSource(EstadoPedido.class)
    void permiteConsultarUnPedidoOnlineEnCualquierEstado(
            EstadoPedido estadoPedido
    ) {
        Pedido pedido = crearPedido();
        pedido.setEstadoPedido(estadoPedido);

        when(pedidoRepository
                .findByIdPedidoAndTipoVentaAndClienteIsNotNull(
                        25,
                        TipoVenta.ONLINE
                ))
                .thenReturn(Optional.of(pedido));
        when(detallePedidoRepository
                .findByPedido_IdPedidoOrderByIdDetallePedidoAsc(25))
                .thenReturn(List.of());

        PedidoDetalleResponse resultado =
                pedidoDetalleService.buscarDetalle(25);

        assertEquals(estadoPedido, resultado.estadoPedido());
        verify(pedidoRepository)
                .findByIdPedidoAndTipoVentaAndClienteIsNotNull(
                        25,
                        TipoVenta.ONLINE
                );
    }

    @Test
    void devuelveDatosCompletosEIndicaStockInsuficiente() {
        Pedido pedido = crearPedido();
        DetallePedido detalle = crearDetalle(pedido, 8, 3);

        when(pedidoRepository
                .findByIdPedidoAndTipoVentaAndClienteIsNotNull(
                        25,
                        TipoVenta.ONLINE
                ))
                .thenReturn(Optional.of(pedido));
        when(detallePedidoRepository
                .findByPedido_IdPedidoOrderByIdDetallePedidoAsc(25))
                .thenReturn(List.of(detalle));

        PedidoDetalleResponse resultado =
                pedidoDetalleService.buscarDetalle(25);

        assertEquals("Transferencia", resultado.medioPago());
        assertEquals("Ana", resultado.cliente().nombre());
        assertEquals("ana@example.com", resultado.cliente().correo());
        assertEquals("Entregar por la tarde", resultado.observacionesCliente());
        assertEquals("Revisar comprobante", resultado.observacionesInternas());
        assertEquals(new BigDecimal("18750.00"), resultado.total());
        assertEquals(1, resultado.productos().size());
        assertEquals(3, resultado.productos().getFirst().stockDisponible());
        assertFalse(resultado.productos().getFirst().stockSuficiente());
    }

    @Test
    void indicaStockSuficienteCuandoCubreLaCantidadSolicitada() {
        Pedido pedido = crearPedido();
        DetallePedido detalle = crearDetalle(pedido, 4, 10);

        when(pedidoRepository
                .findByIdPedidoAndTipoVentaAndClienteIsNotNull(
                        25,
                        TipoVenta.ONLINE
                ))
                .thenReturn(Optional.of(pedido));
        when(detallePedidoRepository
                .findByPedido_IdPedidoOrderByIdDetallePedidoAsc(25))
                .thenReturn(List.of(detalle));

        PedidoDetalleResponse resultado =
                pedidoDetalleService.buscarDetalle(25);

        assertTrue(resultado.productos().getFirst().stockSuficiente());
    }

    @Test
    void noPermiteConsultarUnaVentaLocalDesdeElDetalleOnline() {
        when(pedidoRepository
                .findByIdPedidoAndTipoVentaAndClienteIsNotNull(
                        80,
                        TipoVenta.ONLINE
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                PedidoNoEncontradoException.class,
                () -> pedidoDetalleService.buscarDetalle(80)
        );

        verifyNoInteractions(detallePedidoRepository);
    }

    @Test
    void rechazaUnEnvioSinDireccion() {
        Pedido pedido = crearPedido();
        pedido.setDireccionEnvio(" ");

        when(pedidoRepository
                .findByIdPedidoAndTipoVentaAndClienteIsNotNull(
                        25,
                        TipoVenta.ONLINE
                ))
                .thenReturn(Optional.of(pedido));

        assertThrows(
                PedidoNoEncontradoException.class,
                () -> pedidoDetalleService.buscarDetalle(25)
        );

        verifyNoInteractions(detallePedidoRepository);
    }

    private Pedido crearPedido() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(8);
        cliente.setNombre("Ana");
        cliente.setApellido("Pérez");
        cliente.setTelefono("353-4000000");
        cliente.setCorreo("ana@example.com");

        MedioPago medioPago = new MedioPago();
        medioPago.setIdMedioPago(2);
        medioPago.setNombre("Transferencia");

        Pedido pedido = new Pedido();
        pedido.setIdPedido(25);
        pedido.setNumeroPedido("PED-25");
        pedido.setCliente(cliente);
        pedido.setMedioPago(medioPago);
        pedido.setTipoVenta(TipoVenta.ONLINE);
        pedido.setEstadoPedido(EstadoPedido.PENDIENTE_APROBACION);
        pedido.setEstadoPago(EstadoPago.PENDIENTE);
        pedido.setMedioEntrega(MedioEntrega.ENVIO);
        pedido.setLocalidadEnvio(LocalidadEnvio.VILLA_MARIA);
        pedido.setDireccionEnvio("San Martín 450");
        pedido.setFechaCreacion(LocalDateTime.of(2026, 10, 20, 15, 30));
        pedido.setSubtotal(new BigDecimal("20000.00"));
        pedido.setDescuentoOferta(new BigDecimal("500.00"));
        pedido.setDescuentoMayorista(new BigDecimal("500.00"));
        pedido.setDescuentoCliente(new BigDecimal("250.00"));
        pedido.setTotal(new BigDecimal("18750.00"));
        pedido.setObservacionesCliente("Entregar por la tarde");
        pedido.setObservacionesInternas("Revisar comprobante");
        return pedido;
    }

    private DetallePedido crearDetalle(
            Pedido pedido,
            int cantidad,
            int stockDisponible
    ) {
        Producto producto = new Producto();
        producto.setIdProducto(14);
        producto.setNombre("Vaso térmico");
        producto.setStockActual(stockDisponible);

        DetallePedido detalle = new DetallePedido();
        detalle.setIdDetallePedido(40);
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(new BigDecimal("2500.00"));
        detalle.setDescuentoAplicado(new BigDecimal("250.00"));
        detalle.setSubtotal(new BigDecimal("18000.00"));
        return detalle;
    }
}
