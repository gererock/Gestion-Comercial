package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.ProductoStockBajoResponse;

import java.util.List;

public interface IProductoStockBajoService {

    List<ProductoStockBajoResponse> consultar(
            String idProducto,
            String nombre,
            String idCategoria,
            String idMarca,
            String estado
    );
}
