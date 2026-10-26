package gestion_comercial.service.impl;

import gestion_comercial.dto.request.*;
import gestion_comercial.entity.*;
import gestion_comercial.exception.*;
import gestion_comercial.repository.*;
import gestion_comercial.support.TestDatabaseGuard;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestDatabaseGuard.class)
@Transactional
class VentaLocalClienteServiceTest {
    @Autowired VentaLocalService ventas;
    @Autowired PedidoConfirmacionService online;
    @Autowired ClienteConsultaService consultas;
    @Autowired ClienteRepository clientes;
    @Autowired ProductoRepository productos;
    @Autowired PedidoRepository pedidos;
    @Autowired DetallePedidoRepository detalles;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired MedioPagoRepository medios;
    @Autowired JdbcTemplate jdbc;
    Cliente cliente;
    Producto producto;

    @BeforeEach void preparar() {
        cliente = new Cliente();
        cliente.setNombre("QA US25 Josefina"); cliente.setApellido("Sosa");
        cliente.setDni("45987654"); cliente.setTelefono("3539876543"); cliente.setCorreo("josefina-us25@test.local");
        cliente.setDescuentoPorcentaje(new BigDecimal("10"));
        cliente = clientes.saveAndFlush(cliente);
        producto = new Producto(); producto.setNombre("QA US25 producto"); producto.setPrecioVenta(new BigDecimal("100.00"));
        producto.setPrecioCosto(new BigDecimal("40.00")); producto.setStockActual(100); producto.setPublicadoOnline(false);
        producto = productos.saveAndFlush(producto);
    }
    VentaLocalRequest request(Integer idCliente) {
        return new VentaLocalRequest(List.of(new VentaLocalRequest.Item(producto.getIdProducto(),10)), UUID.randomUUID(), idCliente, medios.findByActivoOrderByNombreAsc(true).getFirst().getIdMedioPago(), EstadoPago.PENDIENTE, "Abona al retirar");
    }
    long cantidad(String tabla) { return jdbc.queryForObject("select count(*) from " + tabla, Long.class); }
    String estadoCliente() { return jdbc.queryForObject("select row_to_json(c)::text from clientes c where id_cliente=?", String.class, cliente.getIdCliente()); }
    void rechazaSinCambios(Integer id, Class<? extends Throwable> tipo) {
        long p = cantidad("pedidos"), d = cantidad("detalle_pedido"), m = cantidad("movimientos_stock");
        assertThrows(tipo, () -> ventas.confirmar("vendedor@gestion.local", request(id)));
        assertEquals(p, cantidad("pedidos")); assertEquals(d, cantidad("detalle_pedido")); assertEquals(m, cantidad("movimientos_stock"));
        assertEquals(100, producto.getStockActual());
    }

    @Test void clienteActivoSeAsociaSinModificarSusDatosNiDescuentosDeLinea() {
        String antes = estadoCliente();
        var respuesta = ventas.confirmar("vendedor@gestion.local", request(cliente.getIdCliente()));
        Pedido pedido = pedidos.findById(respuesta.idPedido()).orElseThrow();
        assertEquals(cliente.getIdCliente(), pedido.getCliente().getIdCliente());
        assertEquals(cliente.getIdCliente(), respuesta.cliente().idCliente());
        assertEquals(new BigDecimal("100.00"), pedido.getDescuentoCliente());
        assertEquals(new BigDecimal("900.00"), respuesta.total());
        var detalle = detalles.findByPedido_IdPedidoOrderByIdDetallePedidoAsc(pedido.getIdPedido()).getFirst();
        assertEquals(new BigDecimal("0.00"), detalle.getDescuentoAplicado());
        assertEquals(new BigDecimal("1000.00"), detalle.getSubtotal());
        clientes.flush(); assertEquals(antes, estadoCliente());
        assertEquals(90, producto.getStockActual());
    }
    @Test void consumidorFinalMantieneUS24() {
        var respuesta = ventas.confirmar("vendedor@gestion.local", request(null));
        Pedido pedido = pedidos.findById(respuesta.idPedido()).orElseThrow();
        assertNull(pedido.getCliente()); assertNull(respuesta.cliente());
        assertEquals(new BigDecimal("0.00"), respuesta.descuentoCliente());
        assertEquals(new BigDecimal("1000.00"), respuesta.total());
        assertEquals(TipoVenta.LOCAL, pedido.getTipoVenta()); assertEquals(EstadoPedido.ENTREGADO, pedido.getEstadoPedido());
        assertEquals(EstadoPago.PENDIENTE,pedido.getEstadoPago()); assertNotNull(pedido.getMedioPago());
    }
    @ParameterizedTest @NullSource @ValueSource(strings={"0","10","100"})
    void porcentajesValidosIncluidoNull(String valor) {
        cliente.setDescuentoPorcentaje(valor == null ? null : new BigDecimal(valor)); clientes.flush();
        var r = ventas.confirmar("vendedor@gestion.local",request(cliente.getIdCliente()));
        BigDecimal esperado = valor == null ? new BigDecimal("0.00") : new BigDecimal(valor).multiply(BigDecimal.TEN).setScale(2);
        assertEquals(esperado,r.descuentoCliente()); assertEquals(new BigDecimal("1000.00").subtract(esperado),r.total());
    }
    @Test void ofertaTienePrioridadYClienteSeAplicaSobreNeto() {
        producto.setEnOferta(true); producto.setPorcentajeDescuentoOferta(new BigDecimal("20"));
        producto.setCantidadMinimaMayorista(2); producto.setPorcentajeDescuentoMayorista(new BigDecimal("30")); productos.flush();
        var r = ventas.confirmar("vendedor@gestion.local",request(cliente.getIdCliente()));
        assertEquals(new BigDecimal("200.00"),r.descuentoOferta()); assertEquals(new BigDecimal("0.00"),r.descuentoMayorista());
        assertEquals(new BigDecimal("80.00"),r.descuentoCliente()); assertEquals(new BigDecimal("720.00"),r.total());
    }
    @Test void mayoristaAntesDeCliente() {
        producto.setCantidadMinimaMayorista(10); producto.setPorcentajeDescuentoMayorista(new BigDecimal("10")); productos.flush();
        var r = ventas.confirmar("vendedor@gestion.local",request(cliente.getIdCliente()));
        assertEquals(new BigDecimal("100.00"),r.descuentoMayorista()); assertEquals(new BigDecimal("90.00"),r.descuentoCliente());
        assertEquals(new BigDecimal("810.00"),r.total());
    }
    @Test void inexistenteNoDejaCambios() { rechazaSinCambios(Integer.MAX_VALUE,ClienteNoEncontradoException.class); }
    @ParameterizedTest @ValueSource(ints={0,-1}) void idInvalidoNoDejaCambios(int id) { rechazaSinCambios(id,IllegalArgumentException.class); }
    @Test void inactivoNoDejaCambios() { cliente.setEstado(false); clientes.flush(); rechazaSinCambios(cliente.getIdCliente(),PedidoConfirmacionException.class); }
    @Test void usaDescuentoActualAlConfirmar() {
        cliente.setDescuentoPorcentaje(new BigDecimal("20")); clientes.flush();
        assertEquals(new BigDecimal("200.00"),ventas.confirmar("vendedor@gestion.local",request(cliente.getIdCliente())).descuentoCliente());
    }
    @Test void mismaClaveYClienteEsIdempotenteAunqueCambieSuEstadoPosteriormente() {
        var req = request(cliente.getIdCliente()); var a = ventas.confirmar("vendedor@gestion.local",req);
        cliente.setEstado(false); cliente.setDescuentoPorcentaje(new BigDecimal("50")); clientes.flush();
        var b = ventas.confirmar("vendedor@gestion.local",req);
        assertEquals(a,b); assertEquals(90,producto.getStockActual());
    }
    @Test void mismaClaveOtroClienteSeRechaza() {
        var req = request(cliente.getIdCliente()); ventas.confirmar("vendedor@gestion.local",req);
        Cliente otro = new Cliente(); otro.setNombre("QA US25 otro"); otro = clientes.saveAndFlush(otro);
        var diferente = new VentaLocalRequest(req.items(),req.claveOperacion(),otro.getIdCliente(), medios.findByActivoOrderByNombreAsc(true).getFirst().getIdMedioPago(), EstadoPago.PENDIENTE, "Abona al retirar");
        assertThrows(PedidoConfirmacionException.class,()->ventas.confirmar("vendedor@gestion.local",diferente));
        assertEquals(90,producto.getStockActual());
    }
    @ParameterizedTest @ValueSource(booleans={true,false}) void clienteYNullNoCompartenOperacion(boolean originalConCliente) {
        var req = request(originalConCliente ? cliente.getIdCliente() : null); ventas.confirmar("vendedor@gestion.local",req);
        var diferente = new VentaLocalRequest(req.items(),req.claveOperacion(),originalConCliente ? null : cliente.getIdCliente(), medios.findByActivoOrderByNombreAsc(true).getFirst().getIdMedioPago(), EstadoPago.PENDIENTE, "Abona al retirar");
        assertThrows(PedidoConfirmacionException.class,()->ventas.confirmar("vendedor@gestion.local",diferente));
        assertEquals(90,producto.getStockActual());
    }
    @ParameterizedTest @ValueSource(strings={"jose","SOSA","4598","353987","josefina-us25@test"})
    void reutilizaBusquedaUS21(String busqueda) {
        assertTrue(consultas.listar(busqueda).stream().anyMatch(c->c.idCliente().equals(cliente.getIdCliente())));
    }
    @ParameterizedTest @ValueSource(strings={"   ","@@@@"}) void busquedaInvalidaUS21(String busqueda) { assertThrows(IllegalArgumentException.class,()->consultas.listar(busqueda)); }
    @Test void listadoDetalleYSinResultadosUS21() {
        assertTrue(consultas.listar("").stream().anyMatch(c->c.idCliente().equals(cliente.getIdCliente())));
        assertEquals(cliente.getIdCliente(),consultas.buscarPorId(cliente.getIdCliente()).idCliente());
        assertTrue(consultas.listar("inexistente-"+UUID.randomUUID()).isEmpty());
    }
    private String asociarUsuarioOnline() {
        Usuario u = new Usuario(); u.setNombre("QA US25 online"); u.setEmail(UUID.randomUUID()+"@test.local");
        u.setEstado(EstadoUsuario.ACTIVO); u.setRol(roles.findByNombre("CLIENTE").orElseThrow()); u.setPasswordHash("test"); u.setFechaAlta(LocalDateTime.now());
        u=usuarios.saveAndFlush(u); cliente.setUsuario(u); clientes.flush(); producto.setPublicadoOnline(true); productos.flush(); return u.getEmail();
    }
    @Test void onlineConservaClienteDescuentoYMedioPago() {
        String email=asociarUsuarioOnline();
        var r=online.confirmar(email,new ConfirmarPedidoRequest(UUID.randomUUID(),List.of(new ItemConfirmarPedidoRequest(producto.getIdProducto(),10)),
                medios.findAll().getFirst().getIdMedioPago(),MedioEntrega.RETIRO_LOCAL,null,null));
        Pedido p=pedidos.findById(r.idPedido()).orElseThrow();
        assertEquals(TipoVenta.ONLINE,p.getTipoVenta()); assertEquals(cliente.getIdCliente(),p.getCliente().getIdCliente());
        assertNotNull(p.getMedioPago()); assertEquals(new BigDecimal("100.00"),p.getDescuentoCliente()); assertEquals(new BigDecimal("900.00"),p.getTotal());
    }
    @Test void onlineSinClienteAutenticadoSeRechaza() {
        assertThrows(PedidoConfirmacionException.class,()->online.confirmar("inexistente@test.local",new ConfirmarPedidoRequest(UUID.randomUUID(),
                List.of(new ItemConfirmarPedidoRequest(producto.getIdProducto(),1)),medios.findAll().getFirst().getIdMedioPago(),MedioEntrega.RETIRO_LOCAL,null,null)));
    }
    @Test void onlineSinMedioPagoSeRechaza() {
        String email=asociarUsuarioOnline();
        assertThrows(PedidoConfirmacionException.class,()->online.confirmar(email,new ConfirmarPedidoRequest(UUID.randomUUID(),
                List.of(new ItemConfirmarPedidoRequest(producto.getIdProducto(),1)),null,MedioEntrega.RETIRO_LOCAL,null,null)));
    }
}
