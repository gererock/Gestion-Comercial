package gestion_comercial.dto.response;

import gestion_comercial.entity.EstadoProducto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoResponse(

        Integer idProducto,

        Integer idCategoria,

        Integer idMarca,

        Integer idGrupoCatalogo,

        String nombre,

        String descripcion,

        BigDecimal precioCosto,

        BigDecimal precioVenta,

        Integer stockActual,

        Integer stockMinimo,

        Integer cantidadMinimaMayorista,

        BigDecimal porcentajeDescuentoMayorista,

        Boolean enOferta,

        BigDecimal porcentajeDescuentoOferta,

        EstadoProducto estado,

        Boolean publicadoOnline,

        LocalDateTime fechaAlta

) {
}