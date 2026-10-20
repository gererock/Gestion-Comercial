
package gestion_comercial.repository;

import gestion_comercial.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository
        extends JpaRepository<Pedido, Integer> {

    Optional<Pedido> findByClaveConfirmacion(
            UUID claveConfirmacion
    );
}
