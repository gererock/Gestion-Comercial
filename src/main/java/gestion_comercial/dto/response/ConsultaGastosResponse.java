package gestion_comercial.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public record ConsultaGastosResponse(

        LocalDate desde,

        LocalDate hasta,

        Integer cantidad,

        BigDecimal total,

        List<GastoConsultaItemResponse> gastos

) {
}