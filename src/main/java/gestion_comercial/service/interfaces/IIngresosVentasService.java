package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.IngresosVentasResponse;
import gestion_comercial.entity.PeriodoIngreso;

import java.time.LocalDate;


public interface IIngresosVentasService {

    IngresosVentasResponse consultarIngresos(

            PeriodoIngreso periodo,

            LocalDate desde,

            LocalDate hasta
    );
}