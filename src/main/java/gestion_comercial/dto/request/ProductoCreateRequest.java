package gestion_comercial.dto.request;

import gestion_comercial.entity.EstadoProducto;

import java.math.BigDecimal;

public record ProductoCreateRequest(

        String nombre,

        String descripcion,

        Integer idCategoria,

        Integer idMarca,

        Integer idGrupoCatalogo,

        BigDecimal precioCosto,

        BigDecimal precioVenta,

        Integer stockActual,

        Integer stockMinimo,

        Integer cantidadMinimaMayorista,

        BigDecimal porcentajeDescuentoMayorista,

        Boolean enOferta,

        BigDecimal porcentajeDescuentoOferta,

        EstadoProducto estado,

        Boolean publicadoOnline

) {
}