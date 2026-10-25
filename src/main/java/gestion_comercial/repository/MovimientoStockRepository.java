
package gestion_comercial.repository;

import gestion_comercial.entity.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovimientoStockRepository
        extends JpaRepository<MovimientoStock, Integer> {

    Optional<MovimientoStock> findByClaveOperacion(
            String claveOperacion
    );
}
