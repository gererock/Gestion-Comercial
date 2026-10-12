package gestion_comercial.repository;

import gestion_comercial.entity.EstadoProducto;
import gestion_comercial.entity.Producto;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CatalogoGrupoProductoRepository
        extends Repository<Producto, Integer> {


    // =========================================
    // OPCIONES PÚBLICAS DE GRUPO - US05
    // =========================================

    @Query("""
            SELECT p
            FROM Producto p

            JOIN FETCH p.grupoCatalogo g

            WHERE
                g.idGrupoCatalogo = :idGrupoCatalogo

            AND
                g.activo = true

            AND
                p.estado = :estado

            AND
                p.publicadoOnline = true

            ORDER BY
                p.nombre ASC,
                p.idProducto ASC
            """)
    List<Producto> buscarOpcionesPublicas(

            @Param("idGrupoCatalogo")
            Integer idGrupoCatalogo,

            @Param("estado")
            EstadoProducto estado
    );
}