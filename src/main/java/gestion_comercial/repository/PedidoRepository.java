package gestion_comercial.repository;

import gestion_comercial.entity.Pedido;

import org.springframework.data.jpa.repository.JpaRepository;


public interface PedidoRepository
        extends JpaRepository<Pedido, Integer> {
}