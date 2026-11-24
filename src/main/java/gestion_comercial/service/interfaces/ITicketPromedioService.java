package gestion_comercial.service.interfaces;

import java.time.LocalDate;

import gestion_comercial.dto.response.TicketPromedioResponse;
import gestion_comercial.entity.PeriodoTicketPromedio;
import gestion_comercial.entity.TipoTicketPromedio;

public interface ITicketPromedioService {
    TicketPromedioResponse consultarTicketPromedio(PeriodoTicketPromedio periodo, TipoTicketPromedio tipo, LocalDate desde,
            LocalDate hasta);
}
