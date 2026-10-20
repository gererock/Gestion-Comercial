package gestion_comercial.repository;

import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.TipoVenta;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface PedidoRepository
        extends JpaRepository<Pedido, Integer> {



    Optional<Pedido> findByClaveConfirmacion(
            UUID claveConfirmacion
    );


    @EntityGraph(
            attributePaths = {
                    "cliente"
            }
    )
    List<Pedido>
    findByTipoVentaAndEstadoPedidoAndClienteIsNotNullOrderByFechaCreacionDesc(
            TipoVenta tipoVenta,
            EstadoPedido estadoPedido
    );


    @EntityGraph(
            attributePaths = {
                    "cliente"
            }
    )
    Optional<Pedido>
    findByIdPedidoAndTipoVentaAndEstadoPedidoAndClienteIsNotNull(
            Integer idPedido,
            TipoVenta tipoVenta,
            EstadoPedido estadoPedido
    );


    @EntityGraph(
            attributePaths = {
                    "cliente",
                    "medioPago"
            }
    )
    Optional<Pedido>
    findByIdPedidoAndTipoVentaAndClienteIsNotNull(
            Integer idPedido,
            TipoVenta tipoVenta
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM Pedido p
            WHERE p.idPedido = :idPedido
            """)
    Optional<Pedido> buscarPorIdParaDecision(
            @Param("idPedido") Integer idPedido
    );


    @EntityGraph(
            attributePaths = {
                    "medioPago"
            }
    )
    List<Pedido> findByCliente_IdClienteAndTipoVentaOrderByFechaCreacionDesc(
            Integer idCliente,
            TipoVenta tipoVenta
    );
}
