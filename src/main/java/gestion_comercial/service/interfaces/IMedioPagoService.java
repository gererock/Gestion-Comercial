package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.MedioPagoRequest;
import gestion_comercial.dto.response.MedioPagoResponse;

import java.util.List;


public interface IMedioPagoService {

    MedioPagoResponse crear(
            MedioPagoRequest request
    );


    MedioPagoResponse buscarPorId(
            Integer idMedioPago
    );


    List<MedioPagoResponse> buscar(
            String nombre,
            Boolean activo
    );


    MedioPagoResponse modificar(
            Integer idMedioPago,
            MedioPagoRequest request
    );


    MedioPagoResponse cambiarEstado(
            Integer idMedioPago,
            Boolean activo
    );


    // =========================================
    // CHECKOUT - US11
    // =========================================

    List<MedioPagoResponse>
    listarActivos();


    MedioPagoResponse buscarActivoPorId(
            Integer idMedioPago
    );
}