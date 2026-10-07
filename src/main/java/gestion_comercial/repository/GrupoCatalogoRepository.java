package gestion_comercial.repository;

import gestion_comercial.entity.GrupoCatalogo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoCatalogoRepository
        extends JpaRepository<GrupoCatalogo, Integer> {
}