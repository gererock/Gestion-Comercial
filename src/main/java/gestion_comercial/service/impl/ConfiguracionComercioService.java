package gestion_comercial.service.impl;

import gestion_comercial.dto.request.MontoMinimoEnvioRequest;
import gestion_comercial.dto.response.ConfiguracionEnvioResponse;

import gestion_comercial.entity.ConfiguracionComercio;

import gestion_comercial.repository.ConfiguracionComercioRepository;

import gestion_comercial.service.interfaces.IConfiguracionComercioService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
public class ConfiguracionComercioService
        implements IConfiguracionComercioService {

    private static final BigDecimal MONTO_MAXIMO =
            new BigDecimal(
                    "9999999999.99"
            );


    private final ConfiguracionComercioRepository
            configuracionComercioRepository;


    public ConfiguracionComercioService(
            ConfiguracionComercioRepository configuracionComercioRepository
    ) {

        this.configuracionComercioRepository =
                configuracionComercioRepository;
    }


    // =========================================
    // OBTENER CONFIGURACIÓN - US61
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionEnvioResponse
    obtenerConfiguracionEnvio() {

        ConfiguracionComercio configuracion =
                obtenerConfiguracion();


        return convertirResponse(
                configuracion
        );
    }


    // =========================================
    // ACTUALIZAR MONTO MÍNIMO - US61
    // =========================================

    @Override
    @Transactional
    public ConfiguracionEnvioResponse
    actualizarMontoMinimoEnvio(
            MontoMinimoEnvioRequest request
    ) {

        BigDecimal monto =
                request.montoMinimoEnvio();


        validarMonto(
                monto
        );


        ConfiguracionComercio configuracion =
                obtenerConfiguracion();


        configuracion.setMontoMinimoEnvio(
                monto
        );


        configuracion.setFechaActualizacion(
                LocalDateTime.now()
        );


        ConfiguracionComercio guardada =
                configuracionComercioRepository
                        .save(
                                configuracion
                        );


        return convertirResponse(
                guardada
        );
    }


    // =========================================
    // OBTENER FILA ÚNICA
    // =========================================

    private ConfiguracionComercio
    obtenerConfiguracion() {

        return configuracionComercioRepository
                .findFirstByOrderByIdConfiguracionAsc()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "No existe la configuración inicial del comercio"
                                )
                );
    }


    // =========================================
    // VALIDAR MONTO
    // =========================================

    private void validarMonto(
            BigDecimal monto
    ) {

        if (
                monto == null
        ) {

            throw new IllegalArgumentException(
                    "El monto mínimo de envío es obligatorio"
            );
        }


        if (
                monto.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {

            throw new IllegalArgumentException(
                    "El monto mínimo de envío no puede ser negativo"
            );
        }


        if (
                monto.compareTo(
                        MONTO_MAXIMO
                ) > 0
        ) {

            throw new IllegalArgumentException(
                    "El monto mínimo de envío supera el límite permitido"
            );
        }


        if (
                monto.scale() > 2
        ) {

            throw new IllegalArgumentException(
                    "El monto mínimo de envío puede tener como máximo 2 decimales"
            );
        }
    }


    // =========================================
    // RESPONSE
    // =========================================

    private ConfiguracionEnvioResponse
    convertirResponse(
            ConfiguracionComercio configuracion
    ) {

        return new ConfiguracionEnvioResponse(

                configuracion
                        .getMontoMinimoEnvio(),

                configuracion
                        .getFechaActualizacion()
        );
    }
}