
package gestion_comercial.repository;

import gestion_comercial.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository
        extends JpaRepository<DetallePedido, Integer> {
}
