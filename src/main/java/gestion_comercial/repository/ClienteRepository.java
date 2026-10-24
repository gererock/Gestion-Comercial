package gestion_comercial.repository;

import gestion_comercial.entity.Cliente;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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


    List<Cliente>
    findAllByOrderByNombreAscApellidoAsc();

    @Query("""
        SELECT c
        FROM Cliente c
        WHERE
            LOWER(c.nombre)
                LIKE LOWER(CONCAT('%', :busqueda, '%'))

            OR LOWER(COALESCE(c.apellido, ''))
                LIKE LOWER(CONCAT('%', :busqueda, '%'))

            OR LOWER(COALESCE(c.dni, ''))
                LIKE LOWER(CONCAT('%', :busqueda, '%'))

            OR LOWER(COALESCE(c.correo, ''))
                LIKE LOWER(CONCAT('%', :busqueda, '%'))

            OR LOWER(COALESCE(c.telefono, ''))
                LIKE LOWER(CONCAT('%', :busqueda, '%'))

        ORDER BY c.nombre ASC, c.apellido ASC
        """)
    List<Cliente> buscarClientes(
            @Param("busqueda") String busqueda
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.usuario WHERE c.idCliente = :idCliente")
    Optional<Cliente> buscarPorIdParaAsociar(
            @Param("idCliente") Integer idCliente
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cliente c WHERE c.idCliente = :idCliente")
    Optional<Cliente> buscarPorIdParaModificarDescuento(
            @Param("idCliente") Integer idCliente
    );

    boolean existsByUsuario_IdUsuario(
            Integer idUsuario
    );
}
