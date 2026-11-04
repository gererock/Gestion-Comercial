package gestion_comercial.service.impl;

import gestion_comercial.entity.*;
import gestion_comercial.repository.*;
import gestion_comercial.exception.PedidoNoEncontradoException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistorialVentaServiceTest {
    @Mock PedidoRepository pedidos;
    @Mock DetallePedidoRepository detalles;
    HistorialVentaService service;
    @BeforeEach void preparar() { service=new HistorialVentaService(pedidos,detalles); }
    static BigDecimal dinero(String n) { return new BigDecimal(n); }
    static Pedido venta() {
        var p=new Pedido(); p.setIdPedido(44); p.setNumeroPedido("US44-44");
        p.setFechaCreacion(LocalDateTime.of(2026,10,20,12,30));
        p.setTipoVenta(TipoVenta.LOCAL); p.setEstadoPedido(EstadoPedido.CONFIRMADO);
        p.setEstadoPago(EstadoPago.PENDIENTE); p.setMedioEntrega(MedioEntrega.RETIRO_LOCAL);
        p.setSubtotal(dinero("250.00")); p.setDescuentoCliente(dinero("10.00"));
        p.setDescuentoMayorista(dinero("15.00")); p.setDescuentoOferta(dinero("5.00")); p.setTotal(dinero("220.00"));
        return p;
    }
    @ParameterizedTest @EnumSource(TipoVenta.class)
    void listadoConservaTodosLosCampos(TipoVenta tipo) {
        var p=venta(); p.setTipoVenta(tipo);
        var c=new Cliente();c.setNombre("Ana");c.setApellido("Perez");p.setCliente(c);
        var m=new MedioPago();m.setNombre("Efectivo");p.setMedioPago(m);
        when(pedidos.findAllByOrderByFechaCreacionDescIdPedidoDesc()).thenReturn(List.of(p));
        var r=service.listarHistorial().getFirst();
        assertAll(()->assertEquals(44,r.idPedido()),()->assertEquals(p.getNumeroPedido(),r.numeroPedido()),
            ()->assertEquals(p.getFechaCreacion(),r.fecha()),()->assertEquals("Ana Perez",r.cliente()),
            ()->assertEquals(tipo,r.tipoVenta()),()->assertEquals(p.getEstadoPedido(),r.estadoPedido()),
            ()->assertEquals(p.getEstadoPago(),r.estadoPago()),()->assertEquals("Efectivo",r.medioPago()),
            ()->assertEquals(p.getMedioEntrega(),r.medioEntrega()),()->assertEquals(dinero("220.00"),r.total()));
        verifyNoInteractions(detalles);
    }
    @ParameterizedTest @CsvSource({"Ana,Perez,,Ana Perez", "Ana,Perez,Empresa SA,Empresa SA", ",Perez,,Perez", ",,,Cliente"})
    void identificacionCliente(String nombre,String apellido,String razon,String esperado) {
        var p=venta();var c=new Cliente();c.setNombre(nombre);c.setApellido(apellido);c.setRazonSocial(razon);p.setCliente(c);
        when(pedidos.findByIdPedido(44)).thenReturn(Optional.of(p));
        when(detalles.findByPedido_IdPedidoOrderByIdDetallePedidoAsc(44)).thenReturn(List.of());
        assertEquals(esperado,service.buscarDetalle(44).cliente());
    }
    @Test void consumidorFinalYMedioPagoNulo() {
        when(pedidos.findAllByOrderByFechaCreacionDescIdPedidoDesc()).thenReturn(List.of(venta()));
        var r=service.listarHistorial().getFirst();assertEquals("Consumidor final",r.cliente());assertEquals("Sin medio de pago",r.medioPago());
    }
    @Test void listadoVacio() {
        when(pedidos.findAllByOrderByFechaCreacionDescIdPedidoDesc()).thenReturn(List.of());assertTrue(service.listarHistorial().isEmpty());
    }
    @ParameterizedTest @EnumSource(TipoVenta.class)
    void detalleConservaProductosPreciosHistoricosDescuentosYTotal(TipoVenta tipo) {
        var p=venta();p.setTipoVenta(tipo);var producto=new Producto();producto.setIdProducto(7);producto.setNombre("Producto US44");producto.setPrecioVenta(dinero("999.00"));
        var d=new DetallePedido();d.setProducto(producto);d.setCantidad(2);d.setPrecioUnitario(dinero("100.00"));d.setDescuentoAplicado(dinero("10.00"));d.setSubtotal(dinero("190.00"));
        when(pedidos.findByIdPedido(44)).thenReturn(Optional.of(p));when(detalles.findByPedido_IdPedidoOrderByIdDetallePedidoAsc(44)).thenReturn(List.of(d));
        var r=service.buscarDetalle(44);var item=r.productos().getFirst();
        assertAll(()->assertEquals(44,r.idPedido()),()->assertEquals(p.getNumeroPedido(),r.numeroPedido()),()->assertEquals(p.getFechaCreacion(),r.fecha()),
            ()->assertEquals("Consumidor final",r.cliente()),()->assertEquals(tipo,r.tipoVenta()),()->assertEquals(p.getEstadoPedido(),r.estadoPedido()),
            ()->assertEquals(p.getEstadoPago(),r.estadoPago()),()->assertEquals("Sin medio de pago",r.medioPago()),()->assertEquals(p.getMedioEntrega(),r.medioEntrega()),
            ()->assertEquals(dinero("250.00"),r.subtotal()),()->assertEquals(dinero("10.00"),r.descuentoCliente()),()->assertEquals(dinero("15.00"),r.descuentoMayorista()),
            ()->assertEquals(dinero("5.00"),r.descuentoOferta()),()->assertEquals(dinero("220.00"),r.total()),()->assertEquals(7,item.idProducto()),
            ()->assertEquals("Producto US44",item.producto()),()->assertEquals(2,item.cantidad()),()->assertEquals(dinero("100.00"),item.precioUnitario()),
            ()->assertEquals(dinero("10.00"),item.descuentoAplicado()),()->assertEquals(dinero("190.00"),item.subtotal()));
    }
    @Test void inexistente() {
        when(pedidos.findByIdPedido(99999999)).thenReturn(Optional.empty());
        assertEquals("La venta indicada no existe",assertThrows(PedidoNoEncontradoException.class,()->service.buscarDetalle(99999999)).getMessage());verifyNoInteractions(detalles);
    }
    @ParameterizedTest @NullSource @ValueSource(ints={0,-1})
    void idInvalido(Integer id) {assertThrows(PedidoNoEncontradoException.class,()->service.buscarDetalle(id));verifyNoInteractions(pedidos,detalles);}
}
