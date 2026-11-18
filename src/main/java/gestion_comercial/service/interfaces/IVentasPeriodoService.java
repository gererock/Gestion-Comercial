package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.VentasPeriodoResponse;
import gestion_comercial.entity.PeriodoVenta;


public interface IVentasPeriodoService {

    VentasPeriodoResponse consultarVentasPorPeriodo(
            PeriodoVenta periodo
    );
}