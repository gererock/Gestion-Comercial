package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.ClienteFrecuenteResponse;
import gestion_comercial.entity.OrdenClienteFrecuente;
import gestion_comercial.entity.PeriodoClienteFrecuente;

import java.time.LocalDate;
import java.util.List;


public interface IClienteFrecuenteService {

    List<ClienteFrecuenteResponse> consultarClientesFrecuentes(

            PeriodoClienteFrecuente periodo,

            LocalDate desde,

            LocalDate hasta,

            OrdenClienteFrecuente orden,

            String buscar
    );
}