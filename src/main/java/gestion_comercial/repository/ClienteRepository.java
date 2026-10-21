
package gestion_comercial.repository;

import gestion_comercial.entity.Cliente;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository
        extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByUsuario_EmailIgnoreCase(
            String email
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c FROM Cliente c
        JOIN c.usuario u
        WHERE LOWER(u.email) = LOWER(:email)
        """)
    Optional<Cliente> buscarParaConfirmar(
            @Param("email") String email
    );
}
