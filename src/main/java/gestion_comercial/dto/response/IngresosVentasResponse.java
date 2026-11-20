package gestion_comercial.dto.response;

import gestion_comercial.entity.PeriodoIngreso;

import java.math.BigDecimal;
import java.time.LocalDate;


public record IngresosVentasResponse(

        PeriodoIngreso periodo,

        LocalDate fechaDesde,

        LocalDate fechaHasta,

        long cantidadVentasCobradas,

        BigDecimal montoTotalIngresos

) {
}