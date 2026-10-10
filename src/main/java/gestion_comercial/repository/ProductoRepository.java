package gestion_comercial.repository;

import gestion_comercial.entity.Producto;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository
        extends JpaRepository<Producto, Integer> {
          
}