package gestion_comercial.repository;

import gestion_comercial.entity.ConfiguracionComercio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ConfiguracionComercioRepository
        extends JpaRepository<ConfiguracionComercio, Integer> {

    Optional<ConfiguracionComercio>
    findFirstByOrderByIdConfiguracionAsc();
}