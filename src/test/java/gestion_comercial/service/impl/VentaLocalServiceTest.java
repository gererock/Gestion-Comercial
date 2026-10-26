package gestion_comercial.service.impl;

import gestion_comercial.dto.request.VentaLocalRequest;
import gestion_comercial.entity.*;
import gestion_comercial.exception.*;
import gestion_comercial.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.test.context.ContextConfiguration(initializers = gestion_comercial.support.TestDatabaseGuard.class)
@SpringBootTest
@Transactional
class VentaLocalServiceTest {
 @Autowired VentaLocalService servicio;
 @Autowired ProductoRepository productos;
 @Autowired PedidoRepository pedidos;
 @Autowired DetallePedidoRepository detalles;
 @Autowired MovimientoStockRepository movimientos;
 Producto producto;
 @BeforeEach void preparar() {
 producto=new Producto(); producto.setNombre("QA US24"); producto.setPrecioVenta(new BigDecimal("100.00"));
 producto.setPrecioCosto(new BigDecimal("40.00")); producto.setStockActual(10); producto.setPublicadoOnline(false);
 producto=productos.saveAndFlush(producto);
 }
 VentaLocalRequest request(int cantidad) {return new VentaLocalRequest(List.of(new VentaLocalRequest.Item(producto.getIdProducto(),cantidad)),UUID.randomUUID());}
 @Test void ventaCompletaConResponsableSnapshotYMovimiento() {
 long antes=movimientos.count(); var res=servicio.confirmar("vendedor@gestion.local",request(2));
 Pedido p=pedidos.findById(res.idPedido()).orElseThrow();
 assertEquals(TipoVenta.LOCAL,p.getTipoVenta()); assertEquals(EstadoPedido.ENTREGADO,p.getEstadoPedido());
 assertEquals(EstadoPago.PENDIENTE,p.getEstadoPago()); assertNull(p.getMedioPago()); assertNull(p.getCliente());
 assertEquals(MedioEntrega.RETIRO_LOCAL,p.getMedioEntrega()); assertEquals("vendedor@gestion.local",p.getVendedorResponsable().getEmail());
 assertEquals(new BigDecimal("200.00"),res.total()); assertEquals(8,producto.getStockActual());
 var d=detalles.findByPedido_IdPedidoOrderByIdDetallePedidoAsc(p.getIdPedido()).getFirst();
 assertEquals(new BigDecimal("40.00"),d.getCostoUnitario());
 assertEquals(antes+1,movimientos.count());
 var m=movimientos.findAll().stream().filter(x->x.getProducto().getIdProducto().equals(producto.getIdProducto())).findFirst().orElseThrow();
 assertEquals("SALIDA",m.getTipoMovimiento()); assertEquals("VENTA_LOCAL",m.getOrigen());
 assertEquals(10,m.getStockAnterior());assertEquals(8,m.getStockPosterior());assertEquals(p.getVendedorResponsable().getIdUsuario(),m.getUsuario().getIdUsuario());
 }
 @Test void administradorPuedeRegistrar() {assertNotNull(servicio.confirmar("admin@gestion.local",request(1)));}
 @Test void idempotenciaNoDuplica() {var req=request(2);var a=servicio.confirmar("vendedor@gestion.local",req);var b=servicio.confirmar("vendedor@gestion.local",req);assertEquals(a,b);assertEquals(8,producto.getStockActual());}
 @Test void claveConOtroContenidoSeRechaza() {var req=request(1);servicio.confirmar("vendedor@gestion.local",req);assertThrows(PedidoConfirmacionException.class,()->servicio.confirmar("vendedor@gestion.local",new VentaLocalRequest(request(2).items(),req.claveOperacion())));}
 @Test void claveDeOtroVendedorSeRechaza() {var req=request(1);servicio.confirmar("vendedor@gestion.local",req);assertThrows(PedidoConfirmacionException.class,()->servicio.confirmar("admin@gestion.local",req));}
 @Test void repetidosSeConsolidan() {var item=new VentaLocalRequest.Item(producto.getIdProducto(),2);var res=servicio.confirmar("vendedor@gestion.local",new VentaLocalRequest(List.of(item,item),UUID.randomUUID()));assertEquals(1,res.items().size());assertEquals(4,res.items().getFirst().cantidad());}
 @ParameterizedTest @ValueSource(ints={0,-1,11}) void cantidadesInvalidas(int cantidad) {assertThrows(RuntimeException.class,()->servicio.confirmar("vendedor@gestion.local",request(cantidad)));}
 @ParameterizedTest @ValueSource(ints={1,9,10}) void cantidadesValidas(int cantidad) {servicio.confirmar("vendedor@gestion.local",request(cantidad));assertEquals(10-cantidad,producto.getStockActual());}
 @Test void vaciaSeRechaza() {assertThrows(IllegalArgumentException.class,()->servicio.confirmar("vendedor@gestion.local",new VentaLocalRequest(List.of(),UUID.randomUUID())));}
 @Test void inexistenteSeRechaza() {assertThrows(ProductoNoEncontradoException.class,()->servicio.confirmar("vendedor@gestion.local",new VentaLocalRequest(List.of(new VentaLocalRequest.Item(Integer.MAX_VALUE,1)),UUID.randomUUID())));}
 @Test void pausadoSeRechaza() {producto.setEstado(EstadoProducto.PAUSADO);productos.flush();assertThrows(PedidoConfirmacionException.class,()->servicio.confirmar("vendedor@gestion.local",request(1)));}
 @Test void discontinuadoSeRechaza() {producto.setEstado(EstadoProducto.DISCONTINUADO);productos.flush();assertThrows(PedidoConfirmacionException.class,()->servicio.confirmar("vendedor@gestion.local",request(1)));}
 @Test void sinStockSeRechaza() {producto.setStockActual(0);productos.flush();assertThrows(PedidoConfirmacionException.class,()->servicio.confirmar("vendedor@gestion.local",request(1)));}
 @Test void ofertaTienePrioridad() {producto.setEnOferta(true);producto.setPorcentajeDescuentoOferta(new BigDecimal("20"));producto.setCantidadMinimaMayorista(2);producto.setPorcentajeDescuentoMayorista(new BigDecimal("30"));var r=servicio.confirmar("vendedor@gestion.local",request(2));assertEquals(new BigDecimal("160.00"),r.total());assertEquals(new BigDecimal("0.00"),r.descuentoMayorista());}
 @Test void mayoristaAlcanzaUmbral() {producto.setCantidadMinimaMayorista(2);producto.setPorcentajeDescuentoMayorista(new BigDecimal("10"));assertEquals(new BigDecimal("180.00"),servicio.confirmar("vendedor@gestion.local",request(2)).total());}
 @Test void mayoristaNoAlcanzaUmbral() {producto.setCantidadMinimaMayorista(2);producto.setPorcentajeDescuentoMayorista(new BigDecimal("10"));assertEquals(new BigDecimal("100.00"),servicio.confirmar("vendedor@gestion.local",request(1)).total());}
 @Test void usaPrecioActual() {producto.setPrecioVenta(new BigDecimal("123.45"));productos.flush();assertEquals(new BigDecimal("246.90"),servicio.confirmar("vendedor@gestion.local",request(2)).total());}
 @Test void busquedaLocalNoExigePublicacionOnline() {assertTrue(servicio.buscar(producto.getIdProducto(),"qa us").stream().anyMatch(p->p.idProducto().equals(producto.getIdProducto())));}
 @Test void numerosUnicos() {var a=servicio.confirmar("vendedor@gestion.local",request(1));var b=servicio.confirmar("vendedor@gestion.local",request(1));assertNotEquals(a.numeroPedido(),b.numeroPedido());}
}
