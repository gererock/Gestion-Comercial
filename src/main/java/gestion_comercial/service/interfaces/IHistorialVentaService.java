package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.HistorialVentaDetalleResponse;
import gestion_comercial.dto.response.HistorialVentaResponse;
import gestion_comercial.entity.TipoVenta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IHistorialVentaService {

    List<HistorialVentaResponse> listarHistorial(

            LocalDate fecha,

            LocalDate desde,

            LocalDate hasta,

            String cliente,

            BigDecimal montoMinimo,

            BigDecimal montoMaximo,

            TipoVenta tipoVenta);

    HistorialVentaDetalleResponse buscarDetalle(
            Integer idPedido);
}