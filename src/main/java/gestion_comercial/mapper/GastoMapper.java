package gestion_comercial.mapper;

import gestion_comercial.dto.request.GastoCreateRequest;
import gestion_comercial.dto.response.GastoResponse;
import gestion_comercial.entity.Gasto;
import gestion_comercial.entity.MedioPago;

public final class GastoMapper {

    private GastoMapper() {
    }


    public static GastoResponse toResponse(
            Gasto gasto
    ) {

        MedioPago medioPago =
                gasto.getMedioPago();


        String nombreUsuario =
                gasto.getUsuario().getNombre();


        if (
                gasto.getUsuario().getApellido() != null
                &&
                !gasto.getUsuario().getApellido().isBlank()
        ) {

            nombreUsuario +=
                    " " + gasto.getUsuario().getApellido();
        }


        return new GastoResponse(

                gasto.getIdGasto(),

                gasto.getCategoriaGasto()
                        .getIdCategoriaGasto(),

                gasto.getCategoriaGasto()
                        .getNombre(),

                gasto.getUsuario()
                        .getIdUsuario(),

                nombreUsuario,

                medioPago != null
                        ? medioPago.getIdMedioPago()
                        : null,

                medioPago != null
                        ? medioPago.getNombre()
                        : null,

                gasto.getDescripcion(),

                gasto.getMonto(),

                gasto.getFecha(),

                gasto.getObservaciones()
        );
    }
}
