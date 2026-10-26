package gestion_comercial.service.impl;
import gestion_comercial.dto.request.VentaLocalRequest;
import gestion_comercial.entity.*;
import gestion_comercial.repository.*;
import gestion_comercial.support.TestDatabaseGuard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers=TestDatabaseGuard.class)
class VentaLocalClienteAtomicidadTest {
    @Autowired VentaLocalService ventas;
    @Autowired ClienteRepository clientes;
    @Autowired ProductoRepository productos;
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean MovimientoStockRepository movimientos;
    private Producto producto() {
        Producto p=new Producto();p.setNombre("QA US25 rollback "+UUID.randomUUID());p.setPrecioVenta(new BigDecimal("100.00"));p.setStockActual(10);p.setPublicadoOnline(false);
        return productos.saveAndFlush(p);
    }
    @Test void falloConClienteRevierteTodaLaVentaYSuAsociacion() {
        Cliente c=new Cliente();c.setNombre("QA US25 rollback");c.setDescuentoPorcentaje(new BigDecimal("10"));c=clientes.saveAndFlush(c);
        Producto a=producto(),b=producto();UUID clave=UUID.randomUUID();
        String antes=jdbc.queryForObject("select row_to_json(c)::text from clientes c where id_cliente=?",String.class,c.getIdCliente());
        var request=new VentaLocalRequest(List.of(new VentaLocalRequest.Item(a.getIdProducto(),2),new VentaLocalRequest.Item(b.getIdProducto(),3)),clave,c.getIdCliente());
        doThrow(new IllegalStateException("Fallo controlado US25")).when(movimientos).save(argThat(m->m!=null&&m.getProducto().getIdProducto().equals(b.getIdProducto())));
        try {
            assertThrows(IllegalStateException.class,()->ventas.confirmar("vendedor@gestion.local",request));
            assertEquals(0,jdbc.queryForObject("select count(*) from pedidos where clave_confirmacion=?",Integer.class,clave));
            assertEquals(0,jdbc.queryForObject("select count(*) from detalle_pedido where id_producto in (?,?)",Integer.class,a.getIdProducto(),b.getIdProducto()));
            assertEquals(0,jdbc.queryForObject("select count(*) from movimientos_stock where id_producto in (?,?)",Integer.class,a.getIdProducto(),b.getIdProducto()));
            assertEquals(10,productos.findById(a.getIdProducto()).orElseThrow().getStockActual());assertEquals(10,productos.findById(b.getIdProducto()).orElseThrow().getStockActual());
            assertEquals(antes,jdbc.queryForObject("select row_to_json(c)::text from clientes c where id_cliente=?",String.class,c.getIdCliente()));
        } finally {
            reset(movimientos);
            jdbc.update("delete from movimientos_stock where id_producto in (?,?)",a.getIdProducto(),b.getIdProducto());
            jdbc.update("delete from detalle_pedido where id_pedido in (select id_pedido from pedidos where clave_confirmacion=?)",clave);
            jdbc.update("delete from pedidos where clave_confirmacion=?",clave);
            productos.deleteById(a.getIdProducto());productos.deleteById(b.getIdProducto());clientes.deleteById(c.getIdCliente());
        }
    }
}
