package gestion_comercial.mapper;

import gestion_comercial.dto.response.MedioPagoResponse;

import gestion_comercial.entity.MedioPago;


public final class MedioPagoMapper {

    private MedioPagoMapper() {
    }


    public static MedioPagoResponse toResponse(
            MedioPago medioPago
    ) {

        return new MedioPagoResponse(

                medioPago.getIdMedioPago(),

                medioPago.getNombre(),

                medioPago.getActivo()
        );
    }
}