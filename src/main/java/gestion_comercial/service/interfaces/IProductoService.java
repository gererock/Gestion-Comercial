package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.ProductoCreateRequest;
import gestion_comercial.dto.request.ProductoUpdateRequest;
import gestion_comercial.dto.response.ProductoResponse;

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
}