package gestion_comercial.controller;

import gestion_comercial.dto.request.MedioPagoRequest;
import gestion_comercial.dto.response.MedioPagoResponse;

import gestion_comercial.service.interfaces.IMedioPagoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/medios-pago")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class MedioPagoController {

    private final IMedioPagoService
            medioPagoService;


    public MedioPagoController(
            IMedioPagoService medioPagoService
    ) {

        this.medioPagoService =
                medioPagoService;
    }


    // =========================================
    // CREAR - US59
    // =========================================

    @PostMapping
    public ResponseEntity<MedioPagoResponse> crear(

            @Valid
            @RequestBody
            MedioPagoRequest request
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        medioPagoService.crear(
                                request
                        )
                );
    }


    // =========================================
    // LISTAR / BUSCAR - US59
    // =========================================

    @GetMapping
    public ResponseEntity<List<MedioPagoResponse>> buscar(

            @RequestParam(required = false)
            String nombre,

            @RequestParam(required = false)
            Boolean activo
    ) {

        return ResponseEntity.ok(
                medioPagoService.buscar(

                        nombre,

                        activo
                )
        );
    }


    // =========================================
    // OBTENER POR ID - US59
    // =========================================

    @GetMapping("/{idMedioPago}")
    public ResponseEntity<MedioPagoResponse> buscarPorId(

            @PathVariable
            Integer idMedioPago
    ) {

        return ResponseEntity.ok(
                medioPagoService.buscarPorId(
                        idMedioPago
                )
        );
    }


    // =========================================
    // MODIFICAR - US59
    // =========================================

    @PutMapping("/{idMedioPago}")
    public ResponseEntity<MedioPagoResponse> modificar(

            @PathVariable
            Integer idMedioPago,

            @Valid
            @RequestBody
            MedioPagoRequest request
    ) {

        return ResponseEntity.ok(
                medioPagoService.modificar(

                        idMedioPago,

                        request
                )
        );
    }


    // =========================================
    // ACTIVAR - US59
    // =========================================

    @PatchMapping("/{idMedioPago}/activar")
    public ResponseEntity<MedioPagoResponse> activar(

            @PathVariable
            Integer idMedioPago
    ) {

        return ResponseEntity.ok(
                medioPagoService.cambiarEstado(

                        idMedioPago,

                        true
                )
        );
    }


    // =========================================
    // DESACTIVAR - US59
    // =========================================

    @PatchMapping("/{idMedioPago}/desactivar")
    public ResponseEntity<MedioPagoResponse> desactivar(

            @PathVariable
            Integer idMedioPago
    ) {

        return ResponseEntity.ok(
                medioPagoService.cambiarEstado(

                        idMedioPago,

                        false
                )
        );
    }
}