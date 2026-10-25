
package gestion_comercial.repository;

import gestion_comercial.entity.MovimientoStock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovimientoStockRepository
        extends JpaRepository<MovimientoStock, Integer> {

    Optional<MovimientoStock> findByClaveOperacion(
            String claveOperacion
    );

    @Query("""
            SELECT m
            FROM MovimientoStock m
            JOIN FETCH m.producto p
            JOIN FETCH m.usuario u
            WHERE (:idProducto IS NULL OR p.idProducto = :idProducto)
              AND (:tipoMovimiento IS NULL OR m.tipoMovimiento = :tipoMovimiento)
              AND (:origen IS NULL OR m.origen = :origen)
              AND (:filtrarFechaDesde = false OR m.fecha >= :fechaDesde)
              AND (:filtrarFechaHasta = false OR m.fecha < :fechaHastaExclusiva)
            ORDER BY m.fecha DESC, m.idMovimientoStock DESC
            """)
    List<MovimientoStock> consultar(
            @Param("idProducto") Integer idProducto,
            @Param("tipoMovimiento") String tipoMovimiento,
            @Param("origen") String origen,
            @Param("filtrarFechaDesde") boolean filtrarFechaDesde,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("filtrarFechaHasta") boolean filtrarFechaHasta,
            @Param("fechaHastaExclusiva") LocalDateTime fechaHastaExclusiva
    );
}
