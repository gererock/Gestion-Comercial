
package gestion_comercial.repository;

import gestion_comercial.entity.DetallePedido;
import gestion_comercial.entity.EstadoPedido;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface DetallePedidoRepository
                extends JpaRepository<DetallePedido, Integer> {

        @EntityGraph(attributePaths = "producto")
        List<DetallePedido> findByPedido_IdPedidoOrderByIdDetallePedidoAsc(
                        Integer idPedido);

        @Query("""
                        SELECT d
                        FROM DetallePedido d
                        JOIN d.pedido p
                        WHERE p.fechaCreacion >= :desde
                        AND p.fechaCreacion < :hasta
                        AND p.estadoPedido NOT IN :estadosExcluidos
                        """)
        List<DetallePedido> buscarDetallesVentasValidasPorPeriodo(

                        @Param("desde") LocalDateTime desde,

                        @Param("hasta") LocalDateTime hasta,

                        @Param("estadosExcluidos") Collection<EstadoPedido> estadosExcluidos);
}
