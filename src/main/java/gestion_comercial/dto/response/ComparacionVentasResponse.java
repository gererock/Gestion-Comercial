package gestion_comercial.dto.response;

import gestion_comercial.entity.PeriodoComparacionVenta;

import java.math.BigDecimal;
import java.time.LocalDate;


public record ComparacionVentasResponse(

        PeriodoComparacionVenta periodo,

        LocalDate fechaDesde,

        LocalDate fechaHasta,

        long cantidadVentasOnline,

        BigDecimal montoTotalOnline,

        long cantidadVentasLocales,

        BigDecimal montoTotalLocal

) {
}