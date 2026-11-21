package gestion_comercial.repository;

import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.TipoVenta;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository
                extends JpaRepository<Pedido, Integer>, JpaSpecificationExecutor<Pedido> {

        @Override
        @EntityGraph(attributePaths = { "cliente.usuario.rol", "medioPago" })
        List<Pedido> findAll(Specification<Pedido> specification,
                        Sort sort);

        Optional<Pedido> findByClaveConfirmacion(
                        UUID claveConfirmacion);

        @EntityGraph(attributePaths = {
                        "cliente"
        })
        List<Pedido> findByTipoVentaAndEstadoPedidoAndClienteIsNotNullOrderByFechaCreacionDesc(
                        TipoVenta tipoVenta,
                        EstadoPedido estadoPedido);

        @EntityGraph(attributePaths = {
                        "cliente"
        })
        Optional<Pedido> findByIdPedidoAndTipoVentaAndEstadoPedidoAndClienteIsNotNull(
                        Integer idPedido,
                        TipoVenta tipoVenta,
                        EstadoPedido estadoPedido);

        @EntityGraph(attributePaths = {
                        "cliente",
                        "medioPago"
        })
        Optional<Pedido> findByIdPedidoAndTipoVentaAndClienteIsNotNull(
                        Integer idPedido,
                        TipoVenta tipoVenta);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT p
                        FROM Pedido p
                        WHERE p.idPedido = :idPedido
                        """)
        Optional<Pedido> buscarPorIdParaDecision(
                        @Param("idPedido") Integer idPedido);

        @EntityGraph(attributePaths = {
                        "medioPago"
        })
        List<Pedido> findByCliente_IdClienteAndTipoVentaOrderByFechaCreacionDesc(
                        Integer idCliente,
                        TipoVenta tipoVenta);

        @EntityGraph(attributePaths = {
                        "medioPago"
        })
        Optional<Pedido> findByIdPedidoAndCliente_IdClienteAndTipoVenta(
                        Integer idPedido,
                        Integer idCliente,
                        TipoVenta tipoVenta);

        @EntityGraph(attributePaths = {
                        "medioPago"
        })
        List<Pedido> findByCliente_IdClienteOrderByFechaCreacionDescIdPedidoDesc(
                        Integer idCliente);

        @EntityGraph(attributePaths = {
                        "medioPago",
                        "cliente"
        })
        Optional<Pedido> findByIdPedidoAndCliente_IdCliente(
                        Integer idPedido,
                        Integer idCliente);

        @EntityGraph(attributePaths = {
                        "cliente",
                        "medioPago"
        })
        List<Pedido> findAllByOrderByFechaCreacionDescIdPedidoDesc();

        Optional<Pedido> findByIdPedido(
                        Integer idPedido);

        @Query("""
                        SELECT p
                        FROM Pedido p
                        WHERE p.fechaCreacion >= :desde
                        AND p.fechaCreacion < :hasta
                        AND p.estadoPedido NOT IN :estadosExcluidos
                        ORDER BY p.fechaCreacion ASC, p.idPedido ASC
                        """)
        List<Pedido> buscarVentasValidasPorPeriodo(

                        @Param("desde") LocalDateTime desde,

                        @Param("hasta") LocalDateTime hasta,

                        @Param("estadosExcluidos") Collection<EstadoPedido> estadosExcluidos);

        @Query("""
                        SELECT p
                        FROM Pedido p
                        WHERE p.fechaCreacion >= :desde
                        AND p.fechaCreacion < :hasta
                        AND p.estadoPago = :estadoPago
                        AND p.estadoPedido NOT IN :estadosExcluidos
                        ORDER BY p.fechaCreacion ASC, p.idPedido ASC
                        """)
        List<Pedido> buscarIngresosPorPeriodo(

                        @Param("desde") LocalDateTime desde,

                        @Param("hasta") LocalDateTime hasta,

                        @Param("estadoPago") EstadoPago estadoPago,

                        @Param("estadosExcluidos") Collection<EstadoPedido> estadosExcluidos);

}
