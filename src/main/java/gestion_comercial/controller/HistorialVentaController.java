package gestion_comercial.controller;

import gestion_comercial.dto.response.HistorialVentaDetalleResponse;
import gestion_comercial.dto.response.HistorialVentaResponse;
import gestion_comercial.service.interfaces.IHistorialVentaService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ventas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class HistorialVentaController {

    private final IHistorialVentaService historialVentaService;

    @GetMapping
    public ResponseEntity<List<HistorialVentaResponse>> listarHistorial() {

        List<HistorialVentaResponse> ventas = historialVentaService.listarHistorial();

        return ResponseEntity.ok(
                ventas);
    }

    @GetMapping("/{idPedido}")
    public ResponseEntity<HistorialVentaDetalleResponse> buscarDetalle(
            @PathVariable Integer idPedido) {

        HistorialVentaDetalleResponse venta = historialVentaService.buscarDetalle(
                idPedido);

        return ResponseEntity.ok(
                venta);
    }
}