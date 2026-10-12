package gestion_comercial.repository;

import gestion_comercial.entity.EstadoProducto;
import gestion_comercial.entity.Producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.List;


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
}