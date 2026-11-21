package gestion_comercial.controller;

import gestion_comercial.dto.response.ComparacionVentasResponse;
import gestion_comercial.entity.PeriodoComparacionVenta;
import gestion_comercial.service.interfaces.IComparacionVentasService;

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
@RequestMapping("/api/admin/comparacion-ventas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class ComparacionVentasController {


    private final IComparacionVentasService
            comparacionVentasService;


    @GetMapping
    public ResponseEntity<ComparacionVentasResponse> comparar(

            @RequestParam(required = false)
            PeriodoComparacionVenta periodo,


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


        ComparacionVentasResponse respuesta =
                comparacionVentasService
                        .compararVentas(
                                periodo,
                                desde,
                                hasta
                        );


        return ResponseEntity.ok(
                respuesta
        );
    }
}