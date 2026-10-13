package gestion_comercial.dto.response;

import java.math.BigDecimal;
import java.util.List;


public record CatalogoProductoDetalleResponse(

        Integer idProducto,

        String nombre,

        String descripcion,

        BigDecimal precioVenta,

        Boolean disponible,

        Integer stockDisponible,

        Boolean enOferta,

        BigDecimal porcentajeDescuentoOferta,

        BigDecimal precioOferta,

        Boolean tieneDescuentoMayorista,

        Integer cantidadMinimaMayorista,

        BigDecimal porcentajeDescuentoMayorista,

        Integer idCategoria,

        String categoria,

        Integer idMarca,

        String marca,

        Integer idGrupoCatalogo,

        String grupoCatalogo,

        List<CatalogoImagenResponse> imagenes

) {
}