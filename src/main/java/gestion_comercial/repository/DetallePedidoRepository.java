
package gestion_comercial.repository;

import gestion_comercial.entity.DetallePedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository
        extends JpaRepository<DetallePedido, Integer> {

    @EntityGraph(attributePaths = "producto")
    List<DetallePedido> findByPedido_IdPedidoOrderByIdDetallePedidoAsc(
            Integer idPedido
    );
}
