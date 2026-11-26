package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.ProductoMasVendidoResponse;
import gestion_comercial.entity.PeriodoProductoVendido;

import java.time.LocalDate;
import java.util.List;


public interface IProductoMasVendidoService {

    List<ProductoMasVendidoResponse> consultarProductosMasVendidos(

            PeriodoProductoVendido periodo,

            LocalDate desde,

            LocalDate hasta
    );
}