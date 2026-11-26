package gestion_comercial.dto.response;

import java.math.BigDecimal;


public record ProductoMasVendidoResponse(

        Integer idProducto,

        String nombre,

        String categoria,

        String marca,

        long cantidadVendida,

        BigDecimal totalVendido

) {
}