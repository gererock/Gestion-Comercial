package gestion_comercial.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import gestion_comercial.entity.Marca;

public interface MarcaRespository extends JpaRepository<Marca, Integer> {

    
} 
