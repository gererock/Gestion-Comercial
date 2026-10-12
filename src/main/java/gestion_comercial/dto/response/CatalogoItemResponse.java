package gestion_comercial.dto.response;

import gestion_comercial.entity.TipoItemCatalogo;

import java.math.BigDecimal;


public record CatalogoItemResponse(

        TipoItemCatalogo tipo,

        Integer idProducto,

        Integer idGrupoCatalogo,

        String nombre,

        String imagenUrl,

        BigDecimal precio,

        Boolean precioDesde,

        Boolean disponible,

        Integer cantidadOpciones

) {
}