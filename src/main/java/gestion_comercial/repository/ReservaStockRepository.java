
package gestion_comercial.repository;

import gestion_comercial.entity.ReservaStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaStockRepository
        extends JpaRepository<ReservaStock, Integer> {
}
