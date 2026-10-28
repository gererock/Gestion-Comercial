package gestion_comercial.repository;

import gestion_comercial.entity.CategoriaGasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaGastoRepository
        extends JpaRepository<CategoriaGasto, Integer> {

    boolean existsByNombreIgnoreCase(
            String nombre
    );

    boolean existsByNombreIgnoreCaseAndIdCategoriaGastoNot(
            String nombre,
            Integer idCategoriaGasto
    );

    List<CategoriaGasto> findByNombreContainingIgnoreCase(
            String nombre
    );

    List<CategoriaGasto> findByActiva(
            Boolean activa
    );

    List<CategoriaGasto> findByNombreContainingIgnoreCaseAndActiva(
            String nombre,
            Boolean activa
    );
}