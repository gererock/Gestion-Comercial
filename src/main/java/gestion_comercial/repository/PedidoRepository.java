package gestion_comercial.repository;

import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.TipoVenta;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}