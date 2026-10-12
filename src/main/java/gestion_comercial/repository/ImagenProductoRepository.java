package gestion_comercial.repository;

import gestion_comercial.entity.ImagenProducto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ImagenProductoRepository
        extends JpaRepository<ImagenProducto, Integer> {

    List<ImagenProducto>
    findByProducto_IdProductoOrderByOrdenAscIdImagenAsc(
            Integer idProducto
    );


    Optional<ImagenProducto>
    findByIdImagenAndProducto_IdProducto(
            Integer idImagen,
            Integer idProducto
    );


    Optional<ImagenProducto>
    findFirstByProducto_IdProductoAndPrincipalTrue(
            Integer idProducto
    );


    @Query("""
            SELECT MAX(i.orden)
            FROM ImagenProducto i
            WHERE i.producto.idProducto = :idProducto
            """)
    Integer obtenerOrdenMaximo(
            @Param("idProducto")
            Integer idProducto
    );
}