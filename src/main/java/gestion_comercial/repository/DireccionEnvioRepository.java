package gestion_comercial.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import gestion_comercial.entity.DireccionEnvioCliente;

public interface DireccionEnvioRepository extends JpaRepository<DireccionEnvioCliente, Integer> {
    List<DireccionEnvioCliente> findByCliente_IdClienteOrderByIdDireccionEnvioDesc(
            Integer idCliente);

    Optional<DireccionEnvioCliente> findByIdDireccionEnvioAndCliente_IdCliente(
            Integer idDireccionEnvio,
            Integer idCliente);
}
