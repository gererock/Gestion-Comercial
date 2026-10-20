
package gestion_comercial.repository;

import gestion_comercial.entity.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoStockRepository
        extends JpaRepository<MovimientoStock, Integer> {
}
