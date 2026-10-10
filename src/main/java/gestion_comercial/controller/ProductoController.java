package gestion_comercial.controller;

import gestion_comercial.dto.request.ProductoCreateRequest;
import gestion_comercial.dto.request.ProductoUpdateRequest;
import gestion_comercial.dto.response.ProductoResponse;
import gestion_comercial.service.interfaces.IProductoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

        private final IProductoService productoService;

        public ProductoController(
                        IProductoService productoService) {

                this.productoService = productoService;
        }

        @PostMapping
        public ResponseEntity<ProductoResponse> crear(
                        @Valid @RequestBody ProductoCreateRequest request) {

                ProductoResponse producto = productoService.crear(
                                request);

                return ResponseEntity
                                .status(
                                                HttpStatus.CREATED)
                                .body(
                                                producto);
        }

        @PutMapping("/{id}")
        public ResponseEntity<ProductoResponse> modificar(@Valid @RequestBody ProductoUpdateRequest request,
                        @PathVariable Integer id) {
                return ResponseEntity.ok(productoService.modificar(id, request));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ProductoResponse> obtenerPorId(
                        @PathVariable Integer id) {

                ProductoResponse producto = productoService.obtenerPorId(
                                id);

                return ResponseEntity.ok(
                                producto);
        }
}