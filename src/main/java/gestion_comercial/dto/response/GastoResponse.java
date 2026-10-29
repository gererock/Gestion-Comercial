package gestion_comercial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;


public record GastoResponse(

        Integer idGasto,

        Integer idCategoriaGasto,

        String categoria,

        Integer idUsuario,

        String usuario,

        Integer idMedioPago,

        String medioPago,

        String descripcion,

        BigDecimal monto,

        LocalDate fecha,

        String observaciones

) {
}