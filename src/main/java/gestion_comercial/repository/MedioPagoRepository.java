package gestion_comercial.repository;

import gestion_comercial.entity.MedioPago;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface MedioPagoRepository
        extends JpaRepository<MedioPago, Integer> {


    boolean existsByNombreIgnoreCase(
            String nombre
    );


    boolean existsByNombreIgnoreCaseAndIdMedioPagoNot(
            String nombre,
            Integer idMedioPago
    );


    List<MedioPago> findAllByOrderByNombreAsc();


    List<MedioPago>
    findByNombreContainingIgnoreCaseOrderByNombreAsc(
            String nombre
    );


    List<MedioPago>
    findByActivoOrderByNombreAsc(
            Boolean activo
    );


    List<MedioPago>
    findByNombreContainingIgnoreCaseAndActivoOrderByNombreAsc(
            String nombre,
            Boolean activo
    );


    // =========================================
    // CHECKOUT - US11
    // =========================================

    Optional<MedioPago>
    findByIdMedioPagoAndActivoTrue(
            Integer idMedioPago
    );
}