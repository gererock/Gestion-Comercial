package gestion_comercial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gestion_comercial.entity.Marca;

public interface MarcaRespository
        extends JpaRepository<Marca, Integer> {

    Boolean existsByNombreIgnoreCase(
        String nombre
    );

    Boolean existsByNombreIgnoreCaseAndIdNot(
        String nombre,
        Integer id
    );

    List<Marca> findByNombreContainingIgnoreCase(
        String nombre
    );

    List<Marca> findByActiva(
        Boolean activa
    );

    List<Marca>
        findByNombreContainingIgnoreCaseAndActiva(
            String nombre,
            Boolean activa
        );
}