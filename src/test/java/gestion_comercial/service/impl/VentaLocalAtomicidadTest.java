package gestion_comercial.service.impl;

import gestion_comercial.dto.request.VentaLocalRequest;
import gestion_comercial.entity.*;
import gestion_comercial.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Verifica commits y rollbacks reales, sin una transacción envolvente del test. */
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.test.context.ContextConfiguration(initializers = gestion_comercial.support.TestDatabaseGuard.class)
@SpringBootTest
class VentaLocalAtomicidadTest {
 @Autowired MedioPagoRepository medios;
    @Autowired VentaLocalService servicio;
    @Autowired ProductoRepository productos;
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean MovimientoStockRepository movimientos;

    private Producto crear(int stock) {
        Producto p = new Producto();
        p.setNombre("QA US24 atomicidad " + UUID.randomUUID());
        p.setPrecioVenta(new BigDecimal("100.00"));
        p.setStockActual(stock);
        return productos.saveAndFlush(p);
    }

    private VentaLocalRequest request(Producto p, int cantidad) {
        return new VentaLocalRequest(List.of(new VentaLocalRequest.Item(p.getIdProducto(), cantidad)), UUID.randomUUID(), null, medios.findByActivoOrderByNombreAsc(true).getFirst().getIdMedioPago(), EstadoPago.PENDIENTE, "Abona al retirar");
    }

    private void limpiar(Producto p) {
        Integer id = p.getIdProducto();
        List<Integer> pedidos = jdbc.queryForList("select id_pedido from detalle_pedido where id_producto=?", Integer.class, id);
        jdbc.update("delete from movimientos_stock where id_producto=?", id);
        for (Integer pedido : pedidos) {
            jdbc.update("delete from detalle_pedido where id_pedido=?", pedido);
            jdbc.update("delete from pedidos where id_pedido=?", pedido);
        }
        productos.deleteById(id);
    }

    @Test
    void falloEnSegundoMovimientoReviertePedidoDetallesAmbosStocksYMovimientos() {
        Producto a = crear(10), b = crear(10);
        long pedidosAntes = jdbc.queryForObject("select count(*) from pedidos", Long.class);
        doThrow(new IllegalStateException("Fallo controlado US24"))
                .when(movimientos).save(argThat(m -> m != null &&
                        m.getProducto().getIdProducto().equals(b.getIdProducto())));
        try {
            var req = new VentaLocalRequest(List.of(new VentaLocalRequest.Item(a.getIdProducto(), 2),
                    new VentaLocalRequest.Item(b.getIdProducto(), 3)), UUID.randomUUID(), null, medios.findByActivoOrderByNombreAsc(true).getFirst().getIdMedioPago(), EstadoPago.PENDIENTE, "Abona al retirar");
            assertThrows(IllegalStateException.class, () -> servicio.confirmar("vendedor@gestion.local", req));
            assertEquals(pedidosAntes, jdbc.queryForObject("select count(*) from pedidos", Long.class));
            assertEquals(10, productos.findById(a.getIdProducto()).orElseThrow().getStockActual());
            assertEquals(10, productos.findById(b.getIdProducto()).orElseThrow().getStockActual());
            assertEquals(0, jdbc.queryForObject("select count(*) from movimientos_stock where id_producto in (?,?)", Integer.class, a.getIdProducto(), b.getIdProducto()));
            assertEquals(0, jdbc.queryForObject("select count(*) from detalle_pedido where id_producto in (?,?)", Integer.class, a.getIdProducto(), b.getIdProducto()));
        } finally {
            reset(movimientos);
            limpiar(a);
            limpiar(b);
        }
    }

    @Test
    void dosResponsablesNoPuedenVenderLasMismasUltimasUnidades() throws Exception {
        Producto p = crear(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            List<Future<Boolean>> resultados = new ArrayList<>();
            for (String email : List.of("vendedor@gestion.local", "admin@gestion.local")) {
                resultados.add(pool.submit(() -> {
                    inicio.await();
                    try { servicio.confirmar(email, request(p, 2)); return true; }
                    catch (gestion_comercial.exception.PedidoConfirmacionException error) { return false; }
                }));
            }
            inicio.countDown();
            int exitos = 0;
            for (Future<Boolean> f : resultados) if (f.get(20, TimeUnit.SECONDS)) exitos++;
            assertEquals(1, exitos);
            assertEquals(0, productos.findById(p.getIdProducto()).orElseThrow().getStockActual());
            assertEquals(1, jdbc.queryForObject("select count(*) from movimientos_stock where id_producto=?", Integer.class, p.getIdProducto()));
        } finally {
            pool.shutdownNow();
            pool.awaitTermination(20, TimeUnit.SECONDS);
            limpiar(p);
        }
    }
}
