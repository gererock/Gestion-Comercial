package gestion_comercial.controller;

import gestion_comercial.dto.request.ProductoCreateRequest;
import gestion_comercial.dto.response.ProductoResponse;
import gestion_comercial.service.interfaces.IProductoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final IProductoService
            productoService;


    public ProductoController(
            IProductoService productoService
    ) {

        this.productoService =
                productoService;
    }


    @PostMapping
    public ResponseEntity<ProductoResponse> crear(
            @Valid
            @RequestBody
            ProductoCreateRequest request
    ) {

        ProductoResponse producto =
                productoService.crear(
                        request
                );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        producto
                );
    }
}