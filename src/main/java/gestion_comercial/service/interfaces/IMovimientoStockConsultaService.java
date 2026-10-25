package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.MovimientoStockResponse;

import java.util.List;

public interface IMovimientoStockConsultaService {

    List<MovimientoStockResponse> consultar(
            String idProducto,
            String tipoMovimiento,
            String origen,
            String fechaDesde,
            String fechaHasta
    );
}
