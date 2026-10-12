package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.CatalogoFiltrosResponse;
import gestion_comercial.dto.response.CatalogoItemResponse;
import gestion_comercial.dto.response.CatalogoProductoDetalleResponse;

import java.util.List;


public interface ICatalogoPublicoService {

    List<CatalogoItemResponse> listar();


    List<CatalogoItemResponse> listar(
            String nombre,
            Integer idCategoria,
            Integer idMarca
    );


    CatalogoFiltrosResponse obtenerFiltros();


    CatalogoProductoDetalleResponse obtenerDetalle(
            Integer idProducto
    );

}