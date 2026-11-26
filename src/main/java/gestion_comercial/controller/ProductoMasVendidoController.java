package gestion_comercial.controller;

import gestion_comercial.dto.response.ProductoMasVendidoResponse;
import gestion_comercial.entity.PeriodoProductoVendido;
import gestion_comercial.service.interfaces.IProductoMasVendidoService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/admin/productos-mas-vendidos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class ProductoMasVendidoController {


    private final IProductoMasVendidoService
            productoMasVendidoService;


    @GetMapping
    public ResponseEntity<List<ProductoMasVendidoResponse>> consultar(

            @RequestParam(required = false)
            PeriodoProductoVendido periodo,

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

        List<ProductoMasVendidoResponse> productos =
                productoMasVendidoService
                        .consultarProductosMasVendidos(
                                periodo,
                                desde,
                                hasta
                        );


        return ResponseEntity.ok(
                productos
        );
    }
}