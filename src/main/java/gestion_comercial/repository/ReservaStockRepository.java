
package gestion_comercial.repository;

import gestion_comercial.entity.ReservaStock;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReservaStockRepository
        extends JpaRepository<ReservaStock, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM ReservaStock r
            WHERE r.detallePedido.idDetallePedido = :idDetallePedido
            """)
    Optional<ReservaStock> buscarPorDetalleParaDecision(
            @Param("idDetallePedido") Integer idDetallePedido
    );
}
