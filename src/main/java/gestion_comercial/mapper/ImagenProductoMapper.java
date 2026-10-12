package gestion_comercial.mapper;

import gestion_comercial.dto.response.ImagenProductoResponse;
import gestion_comercial.entity.ImagenProducto;


public final class ImagenProductoMapper {

    private ImagenProductoMapper() {
    }


    public static ImagenProductoResponse toResponse(
            ImagenProducto imagen
    ) {

        return new ImagenProductoResponse(

                imagen.getIdImagen(),

                imagen.getProducto()
                        .getIdProducto(),

                imagen.getUrlImagen(),

                imagen.getOrden(),

                imagen.getPrincipal()
        );
    }
}