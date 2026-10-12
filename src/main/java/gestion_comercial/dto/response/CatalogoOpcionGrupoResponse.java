package gestion_comercial.dto.response;

import java.math.BigDecimal;


public record CatalogoOpcionGrupoResponse(

        Integer idProducto,

        String nombre,

        String imagenUrl,

        BigDecimal precioVenta,

        Boolean disponible

) {
}