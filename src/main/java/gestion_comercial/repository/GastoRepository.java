package gestion_comercial.repository;

import gestion_comercial.entity.Gasto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface GastoRepository
        extends JpaRepository<Gasto, Integer> {


    Optional<Gasto> findByClaveOperacion(
            String claveOperacion
    );


    boolean existsByClaveOperacion(
            String claveOperacion
    );
}