package gestion_comercial.controller;

import gestion_comercial.dto.response.ConfiguracionCheckoutEnvioResponse;
import gestion_comercial.dto.response.ConfiguracionEnvioResponse;

import gestion_comercial.service.interfaces.IConfiguracionComercioService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/public/configuracion-comercio")
public class ConfiguracionComercioPublicoController {

    private final IConfiguracionComercioService
            configuracionComercioService;


    public ConfiguracionComercioPublicoController(
            IConfiguracionComercioService configuracionComercioService
    ) {

        this.configuracionComercioService =
                configuracionComercioService;
    }


    // =========================================
    // CONFIGURACIÓN PÚBLICA DE ENVÍO - US61
    // =========================================

    @GetMapping("/monto-minimo-envio")
    public ResponseEntity<ConfiguracionEnvioResponse>
    obtenerMontoMinimoEnvio() {

        return ResponseEntity.ok(
                configuracionComercioService
                        .obtenerConfiguracionEnvio()
        );
    }

    @GetMapping("/envio")
    public ResponseEntity<ConfiguracionCheckoutEnvioResponse> obtenerCheckout() {
        return ResponseEntity.ok(configuracionComercioService.obtenerCheckout());
    }
}