package gestion_comercial.repository;

import gestion_comercial.entity.EstadoProducto;
import gestion_comercial.entity.Producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;


public interface ProductoRepository
        extends JpaRepository<Producto, Integer> {

    @Query("""
            SELECT p
            FROM Producto p

            LEFT JOIN FETCH p.categoria c
            LEFT JOIN FETCH p.marca m

            WHERE
                (:idProducto IS NULL
                    OR p.idProducto = :idProducto)

            AND
                (:nombre IS NULL
                    OR LOWER(p.nombre)
                    LIKE LOWER(
                        CONCAT('%', :nombre, '%')
                    )
                )

            AND
                (:idCategoria IS NULL
                    OR c.id = :idCategoria)

            AND
                (:idMarca IS NULL
                    OR m.id = :idMarca)

            AND
                (:estado IS NULL
                    OR p.estado = :estado)

            AND
                (:bajoStock = false
                    OR p.stockActual < p.stockMinimo)

            ORDER BY
                p.nombre ASC,
                p.idProducto ASC
            """)
    List<Producto> consultar(

            @Param("idProducto")
            Integer idProducto,

            @Param("nombre")
            String nombre,

            @Param("idCategoria")
            Integer idCategoria,

            @Param("idMarca")
            Integer idMarca,

            @Param("estado")
            EstadoProducto estado,

            @Param("bajoStock")
            boolean bajoStock
    );


    // =========================================
    // CATÁLOGO PÚBLICO - US02
    // =========================================

    @Query("""
            SELECT p
            FROM Producto p

            LEFT JOIN FETCH p.grupoCatalogo g

            WHERE
                p.estado = :estado

            AND
                p.publicadoOnline = true

            ORDER BY
                p.nombre ASC,
                p.idProducto ASC
            """)
    List<Producto> buscarPublicablesParaCatalogo(

            @Param("estado")
            EstadoProducto estado
    );


    // =========================================
    // CATÁLOGO PÚBLICO CON FILTROS - US03
    // =========================================

    @Query("""
            SELECT p
            FROM Producto p

            LEFT JOIN FETCH p.categoria c
            LEFT JOIN FETCH p.marca m
            LEFT JOIN FETCH p.grupoCatalogo g

            WHERE
                p.estado = :estado

            AND
                p.publicadoOnline = true

            AND
                (
                    :nombre IS NULL

                    OR LOWER(p.nombre)
                        LIKE LOWER(
                            CONCAT('%', :nombre, '%')
                        )

                    OR (
                        g IS NOT NULL
                        AND g.activo = true
                        AND LOWER(g.nombreVisible)
                            LIKE LOWER(
                                CONCAT('%', :nombre, '%')
                            )
                    )
                )

            AND
                (
                    :idCategoria IS NULL
                    OR c.id = :idCategoria
                )

            AND
                (
                    :idMarca IS NULL
                    OR m.id = :idMarca
                )

            ORDER BY
                p.nombre ASC,
                p.idProducto ASC
            """)
    List<Producto> buscarPublicablesParaCatalogoFiltrado(

            @Param("estado")
            EstadoProducto estado,

            @Param("nombre")
            String nombre,

            @Param("idCategoria")
            Integer idCategoria,

            @Param("idMarca")
            Integer idMarca
    );


    // =========================================
    // DETALLE PÚBLICO DEL PRODUCTO - US04
    // =========================================

    @Query("""
            SELECT p
            FROM Producto p

            LEFT JOIN FETCH p.categoria c
            LEFT JOIN FETCH p.marca m
            LEFT JOIN FETCH p.grupoCatalogo g

            WHERE
                p.idProducto = :idProducto

            AND
                p.estado = :estado

            AND
                p.publicadoOnline = true
            """)
    Optional<Producto> buscarDetallePublico(

            @Param("idProducto")
            Integer idProducto,

            @Param("estado")
            EstadoProducto estado
    );


            
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
            SELECT p FROM Producto p
            WHERE p.idProducto = :idProducto
            """)
        Optional<Producto> buscarParaReservar(
                @Param("idProducto") Integer idProducto
        );

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
            SELECT p FROM Producto p
            WHERE p.idProducto = :idProducto
            """)
        Optional<Producto> buscarParaReponerStock(
                @Param("idProducto") Integer idProducto
        );

}
