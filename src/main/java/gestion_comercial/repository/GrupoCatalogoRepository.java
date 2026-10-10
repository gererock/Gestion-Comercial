package gestion_comercial.repository;

import gestion_comercial.entity.GrupoCatalogo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoCatalogoRepository
        extends JpaRepository<GrupoCatalogo, Integer> {

    boolean existsByNombreVisibleIgnoreCase(
            String nombreVisible
    );

    boolean existsByNombreVisibleIgnoreCaseAndIdGrupoCatalogoNot(
            String nombreVisible,
            Integer idGrupoCatalogo
    );


    List<GrupoCatalogo>
    findByNombreVisibleContainingIgnoreCase(
            String nombreVisible
    );


    List<GrupoCatalogo>
    findByActivo(
            Boolean activo
    );


    List<GrupoCatalogo>
    findByNombreVisibleContainingIgnoreCaseAndActivo(
            String nombreVisible,
            Boolean activo
    );
}