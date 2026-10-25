package gestion_comercial.controller;

import gestion_comercial.dto.response.MovimientoStockResponse;
import gestion_comercial.service.interfaces.IMovimientoStockConsultaService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stock/movimientos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class MovimientoStockConsultaController {

    private final IMovimientoStockConsultaService movimientoStockConsultaService;

    @GetMapping
    public ResponseEntity<List<MovimientoStockResponse>> consultar(
            @RequestParam(required = false) String idProducto,
            @RequestParam(required = false) String tipoMovimiento,
            @RequestParam(required = false) String origen,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta
    ) {
        return ResponseEntity.ok(
                movimientoStockConsultaService.consultar(
                        idProducto,
                        tipoMovimiento,
                        origen,
                        fechaDesde,
                        fechaHasta
                )
        );
    }
}
