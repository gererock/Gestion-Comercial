package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.ComparacionVentasResponse;
import gestion_comercial.entity.PeriodoComparacionVenta;

import java.time.LocalDate;


public interface IComparacionVentasService {

    ComparacionVentasResponse compararVentas(

            PeriodoComparacionVenta periodo,

            LocalDate desde,

            LocalDate hasta
    );
}