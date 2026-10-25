package gestion_comercial.controller;

import gestion_comercial.dto.response.ProductoStockBajoResponse;
import gestion_comercial.service.interfaces.IProductoStockBajoService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stock/productos-bajo-stock")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class ProductoStockBajoController {

    private final IProductoStockBajoService productoStockBajoService;

    @GetMapping
    public ResponseEntity<List<ProductoStockBajoResponse>> consultar(
            @RequestParam(required = false) String idProducto,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String idCategoria,
            @RequestParam(required = false) String idMarca,
            @RequestParam(required = false) String estado
    ) {
        return ResponseEntity.ok(productoStockBajoService.consultar(
                idProducto,
                nombre,
                idCategoria,
                idMarca,
                estado
        ));
    }
}
