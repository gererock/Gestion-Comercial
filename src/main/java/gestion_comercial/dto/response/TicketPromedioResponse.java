package gestion_comercial.dto.response;

import gestion_comercial.entity.PeriodoTicketPromedio;
import gestion_comercial.entity.TipoTicketPromedio;

import java.math.BigDecimal;
import java.time.LocalDate;


public record TicketPromedioResponse(

        PeriodoTicketPromedio periodo,

        LocalDate fechaDesde,

        LocalDate fechaHasta,

        TipoTicketPromedio tipo,

        long cantidadVentas,

        BigDecimal totalVendido,

        BigDecimal ticketPromedio

) {
}