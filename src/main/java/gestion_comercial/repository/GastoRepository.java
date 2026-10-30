package gestion_comercial.repository;

import gestion_comercial.entity.Gasto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GastoRepository
                extends JpaRepository<Gasto, Integer> {

        Optional<Gasto> findByClaveOperacion(
                        String claveOperacion);

        boolean existsByClaveOperacion(
                        String claveOperacion);

        @Query("""
                        SELECT g
                        FROM Gasto g
                        JOIN FETCH g.categoriaGasto cg
                        JOIN FETCH g.usuario u
                        LEFT JOIN FETCH g.medioPago mp
                        WHERE
                            (CAST(:desde AS date) IS NULL OR g.fecha >= :desde)
                            AND
                            (CAST(:hasta AS date) IS NULL OR g.fecha <= :hasta)
                            AND
                            (
                                :idCategoriaGasto IS NULL
                                OR cg.idCategoriaGasto = :idCategoriaGasto
                            )
                            AND
                            (
                                :idMedioPago IS NULL
                                OR mp.idMedioPago = :idMedioPago
                            )
                        ORDER BY
                            g.fecha DESC,
                            g.idGasto DESC
                        """)
        List<Gasto> buscarPorFiltros(

                        @Param("desde") LocalDate desde,

                        @Param("hasta") LocalDate hasta,

                        @Param("idCategoriaGasto") Integer idCategoriaGasto,

                        @Param("idMedioPago") Integer idMedioPago);
}