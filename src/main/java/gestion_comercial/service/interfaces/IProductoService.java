package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.ProductoCreateRequest;
import gestion_comercial.dto.request.ProductoUpdateRequest;
import gestion_comercial.dto.response.ProductoResponse;
import gestion_comercial.dto.response.ProductoConsultaResponse;
import gestion_comercial.entity.EstadoProducto;

import java.util.List;

public interface IProductoService {

    ProductoResponse crear(
            ProductoCreateRequest request
    );


    ProductoResponse obtenerPorId(
            Integer id
    );


    ProductoResponse modificar(
            Integer id,
            ProductoUpdateRequest request
    );


    ProductoResponse pausar(
            Integer id
    );


    ProductoResponse discontinuar(
            Integer id
    );


    List<ProductoConsultaResponse> consultar(
                Integer idProducto,
                String nombre,
                Integer idCategoria,
                Integer idMarca,
                EstadoProducto estado,
                Boolean bajoStock
        );
}