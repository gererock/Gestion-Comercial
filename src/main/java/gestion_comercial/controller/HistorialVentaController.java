package gestion_comercial.controller;

import gestion_comercial.dto.response.HistorialVentaDetalleResponse;
import gestion_comercial.dto.response.HistorialVentaResponse;
import gestion_comercial.service.interfaces.IHistorialVentaService;
import gestion_comercial.entity.TipoVenta;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/ventas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class HistorialVentaController {

    private final IHistorialVentaService historialVentaService;

    @GetMapping
    public ResponseEntity<List<HistorialVentaResponse>> listarHistorial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) BigDecimal montoMinimo,
            @RequestParam(required = false) BigDecimal montoMaximo,
            @RequestParam(required = false) TipoVenta tipoVenta) {

        List<HistorialVentaResponse> ventas = historialVentaService.listarHistorial(
                fecha, desde, hasta, cliente, montoMinimo, montoMaximo, tipoVenta);

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