package gestion_comercial.repository;

import gestion_comercial.entity.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Usuario> findAllByOrderByNombreAscApellidoAsc();

    @Query("""
        SELECT u
        FROM Usuario u
        WHERE
            LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
            OR LOWER(COALESCE(u.apellido, '')) LIKE LOWER(CONCAT('%', :busqueda, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))
        ORDER BY u.nombre ASC, u.apellido ASC
        """)
    List<Usuario> buscarUsuarios(
            @Param("busqueda") String busqueda
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.idUsuario = :idUsuario")
    Optional<Usuario> buscarPorIdParaAsociar(
            @Param("idUsuario") Integer idUsuario
    );
}
