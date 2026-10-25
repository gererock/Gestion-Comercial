package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoProducto;

import java.math.BigDecimal;

public record ProductoStockBajoResponse(
        Integer idProducto,
        String nombre,
        Integer idCategoria,
        String categoria,
        Integer idMarca,
        String marca,
        Integer stockActual,
        Integer stockMinimo,
        EstadoProducto estado,
        BigDecimal precioVenta,
        Boolean stockBajo,
        String situacionStock
) {
}
