package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoProducto;
import gestion_comercial.entity.EstadoStockProducto;

import java.math.BigDecimal;


public record ProductoConsultaResponse(

        Integer idProducto,

        String nombre,

        Integer idCategoria,

        String categoria,

        Integer idMarca,

        String marca,

        BigDecimal precioVenta,

        Integer stockActual,

        Integer stockMinimo,

        EstadoProducto estado,

        Boolean tieneStock,

        Boolean bajoStock,

        EstadoStockProducto estadoStock

) {
}