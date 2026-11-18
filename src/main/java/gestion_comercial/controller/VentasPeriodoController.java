package gestion_comercial.controller;

import gestion_comercial.dto.response.VentasPeriodoResponse;
import gestion_comercial.entity.PeriodoVenta;
import gestion_comercial.service.interfaces.IVentasPeriodoService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin/ventas/periodo")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class VentasPeriodoController {


    private final IVentasPeriodoService ventasPeriodoService;


    @GetMapping
    public ResponseEntity<VentasPeriodoResponse>
    consultarVentasPorPeriodo(

            @RequestParam
            PeriodoVenta periodo
    ) {

        VentasPeriodoResponse respuesta =
                ventasPeriodoService
                        .consultarVentasPorPeriodo(
                                periodo
                        );


        return ResponseEntity.ok(
                respuesta
        );
    }
}