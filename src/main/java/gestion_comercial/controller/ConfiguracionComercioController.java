package gestion_comercial.controller;

import gestion_comercial.dto.request.MontoMinimoEnvioRequest;
import gestion_comercial.dto.response.ConfiguracionEnvioResponse;

import gestion_comercial.service.interfaces.IConfiguracionComercioService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/configuracion-comercio")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ConfiguracionComercioController {

    private final IConfiguracionComercioService
            configuracionComercioService;


    public ConfiguracionComercioController(
            IConfiguracionComercioService configuracionComercioService
    ) {

        this.configuracionComercioService =
                configuracionComercioService;
    }


    // =========================================
    // OBTENER MONTO ACTUAL - US61
    // =========================================

    @GetMapping("/monto-minimo-envio")
    public ResponseEntity<ConfiguracionEnvioResponse>
    obtenerMontoMinimoEnvio() {

        return ResponseEntity.ok(
                configuracionComercioService
                        .obtenerConfiguracionEnvio()
        );
    }


    // =========================================
    // MODIFICAR MONTO - US61
    // =========================================

    @PutMapping("/monto-minimo-envio")
    public ResponseEntity<ConfiguracionEnvioResponse>
    actualizarMontoMinimoEnvio(

            @Valid
            @RequestBody
            MontoMinimoEnvioRequest request
    ) {

        return ResponseEntity.ok(
                configuracionComercioService
                        .actualizarMontoMinimoEnvio(
                                request
                        )
        );
    }
}