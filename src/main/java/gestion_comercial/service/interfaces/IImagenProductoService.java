package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.ImagenProductoOrdenRequest;
import gestion_comercial.dto.response.ImagenProductoResponse;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface IImagenProductoService {

    List<ImagenProductoResponse> listar(
            Integer idProducto
    );


    ImagenProductoResponse agregar(
            Integer idProducto,
            MultipartFile archivo,
            Integer orden,
            Boolean principal
    );


    ImagenProductoResponse definirPrincipal(
            Integer idProducto,
            Integer idImagen
    );


    ImagenProductoResponse cambiarOrden(
            Integer idProducto,
            Integer idImagen,
            ImagenProductoOrdenRequest request
    );


    void eliminar(
            Integer idProducto,
            Integer idImagen
    );
}