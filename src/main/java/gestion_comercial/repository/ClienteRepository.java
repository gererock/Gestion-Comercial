package gestion_comercial.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import gestion_comercial.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuario_EmailIgnoreCase(
            String email);
}
