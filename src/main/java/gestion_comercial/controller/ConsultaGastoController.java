package gestion_comercial.controller;

import gestion_comercial.dto.response.ConsultaGastosResponse;
import gestion_comercial.entity.PeriodoConsultaGasto;
import gestion_comercial.service.interfaces.IConsultaGastoService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/admin/gastos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class ConsultaGastoController {

    private final IConsultaGastoService
            consultaGastoService;


    @GetMapping
    public ResponseEntity<ConsultaGastosResponse> consultar(

            @RequestParam(required = false)
            PeriodoConsultaGasto periodo,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate desde,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate hasta,

            @RequestParam(required = false)
            Integer idCategoriaGasto,

            @RequestParam(required = false)
            Integer idMedioPago
    ) {

        ConsultaGastosResponse resultado =
                consultaGastoService
                        .consultar(
                                periodo,
                                desde,
                                hasta,
                                idCategoriaGasto,
                                idMedioPago
                        );


        return ResponseEntity.ok(
                resultado
        );
    }
}