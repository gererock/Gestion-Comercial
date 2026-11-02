package gestion_comercial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;


public record GastoConsultaItemResponse(

        Integer idGasto,

        LocalDate fecha,

        Integer idCategoriaGasto,

        String categoria,

        String descripcion,

        BigDecimal monto,

        Integer idMedioPago,

        String medioPago,

        Integer idUsuario,

        String usuario,

        String observaciones

) {
}