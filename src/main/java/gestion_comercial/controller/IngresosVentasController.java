package gestion_comercial.controller;

import gestion_comercial.dto.response.IngresosVentasResponse;
import gestion_comercial.entity.PeriodoIngreso;
import gestion_comercial.service.interfaces.IIngresosVentasService;

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
@RequestMapping("/api/admin/ingresos-ventas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class IngresosVentasController {


    private final IIngresosVentasService
            ingresosVentasService;


    @GetMapping
    public ResponseEntity<IngresosVentasResponse> consultar(

            @RequestParam(required = false)
            PeriodoIngreso periodo,


            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate desde,


            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate hasta
    ) {


        IngresosVentasResponse respuesta =
                ingresosVentasService
                        .consultarIngresos(
                                periodo,
                                desde,
                                hasta
                        );


        return ResponseEntity.ok(
                respuesta
        );
    }
}