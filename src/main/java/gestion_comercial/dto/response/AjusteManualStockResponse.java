package gestion_comercial.dto.response;

import java.time.LocalDateTime;

public record AjusteManualStockResponse(
        Integer idMovimientoStock,
        Integer idProducto,
        String producto,
        Integer idUsuario,
        String tipoMovimiento,
        String origen,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockPosterior,
        LocalDateTime fecha,
        String mensaje
) {
}
