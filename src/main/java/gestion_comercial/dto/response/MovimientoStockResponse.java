package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoProducto;

import java.time.LocalDateTime;

public record MovimientoStockResponse(
        Integer idMovimientoStock,
        Integer idProducto,
        String producto,
        EstadoProducto estadoProducto,
        Integer idUsuario,
        String usuario,
        String emailUsuario,
        String tipoMovimiento,
        String origen,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockPosterior,
        LocalDateTime fecha
) {
}
