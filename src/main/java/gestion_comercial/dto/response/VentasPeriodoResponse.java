package gestion_comercial.dto.response;

import gestion_comercial.entity.PeriodoVenta;

import java.math.BigDecimal;
import java.time.LocalDate;


public record VentasPeriodoResponse(

        PeriodoVenta periodo,

        LocalDate fechaDesde,

        LocalDate fechaHasta,

        long cantidadVentasRealizadas,

        BigDecimal montoTotalVendido,

        long cantidadVentasCobradas,

        BigDecimal montoTotalCobrado

) {
}