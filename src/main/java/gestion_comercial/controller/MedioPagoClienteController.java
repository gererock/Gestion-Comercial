package gestion_comercial.controller;

import gestion_comercial.dto.response.MedioPagoResponse;

import gestion_comercial.service.interfaces.IMedioPagoService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/cliente/medios-pago")
@PreAuthorize("hasRole('CLIENTE')")
public class MedioPagoClienteController {

    private final IMedioPagoService
            medioPagoService;


    public MedioPagoClienteController(
            IMedioPagoService medioPagoService
    ) {

        this.medioPagoService =
                medioPagoService;
    }


    // =========================================
    // LISTAR MEDIOS ACTIVOS - US11
    // =========================================

    @GetMapping
    public ResponseEntity<List<MedioPagoResponse>>
    listarActivos() {

        return ResponseEntity.ok(
                medioPagoService
                        .listarActivos()
        );
    }


    // =========================================
    // REVALIDAR MEDIO SELECCIONADO - US11
    // =========================================

    @GetMapping("/{idMedioPago}")
    public ResponseEntity<MedioPagoResponse>
    buscarActivoPorId(

            @PathVariable
            Integer idMedioPago
    ) {

        return ResponseEntity.ok(
                medioPagoService
                        .buscarActivoPorId(
                                idMedioPago
                        )
        );
    }
}