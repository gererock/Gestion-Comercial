package gestion_comercial.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gestion_comercial.entity.Categoria;

public interface CategoriaRespository extends JpaRepository<Categoria, Integer>{
    Boolean existsByNombreIgnoreCase(String nombre);

    Boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);

    List<Categoria> findByNombreContainingIgnoreCase(String nombre);

    List<Categoria> findByActiva(Boolean activa);

    List<Categoria> findByNombreContainingIgnoreCaseAndActiva(String nombre, Boolean activa);
}
