package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.ConsultaGastosResponse;
import gestion_comercial.entity.PeriodoConsultaGasto;

import java.time.LocalDate;


public interface IConsultaGastoService {

    ConsultaGastosResponse consultar(
            PeriodoConsultaGasto periodo,
            LocalDate desde,
            LocalDate hasta,
            Integer idCategoriaGasto,
            Integer idMedioPago
    );
}