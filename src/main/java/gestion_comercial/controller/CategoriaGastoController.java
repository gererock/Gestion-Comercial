package gestion_comercial.controller;

import gestion_comercial.dto.request.CategoriaGastoCreateRequest;
import gestion_comercial.dto.request.CategoriaGastoUpdateRequest;
import gestion_comercial.dto.response.CategoriaGastoResponse;
import gestion_comercial.service.interfaces.ICategoriaGastoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categorias-gasto")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class CategoriaGastoController {

    private final ICategoriaGastoService categoriaGastoService;

    @PostMapping
    public ResponseEntity<CategoriaGastoResponse> crear(
            @Valid @RequestBody CategoriaGastoCreateRequest request) {

        CategoriaGastoResponse categoria = categoriaGastoService.crear(
                request);

        return ResponseEntity
                .status(
                        HttpStatus.CREATED)
                .body(
                        categoria);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaGastoResponse>> buscar(
            @RequestParam(required = false) String nombre,

            @RequestParam(required = false) Boolean activa) {

        return ResponseEntity.ok(
                categoriaGastoService.buscar(
                        nombre,
                        activa));
    }

    @GetMapping("/{idCategoriaGasto}")
    public ResponseEntity<CategoriaGastoResponse> buscarPorId(
            @PathVariable Integer idCategoriaGasto) {

        return ResponseEntity.ok(
                categoriaGastoService.buscarPorId(
                        idCategoriaGasto));
    }

    @PutMapping("/{idCategoriaGasto}")
    public ResponseEntity<CategoriaGastoResponse> actualizar(
            @PathVariable Integer idCategoriaGasto,

            @Valid @RequestBody CategoriaGastoUpdateRequest request) {

        return ResponseEntity.ok(
                categoriaGastoService.actualizar(
                        idCategoriaGasto,
                        request));
    }

    @PatchMapping("/{idCategoriaGasto}/activar")
    public ResponseEntity<CategoriaGastoResponse> activar(
            @PathVariable Integer idCategoriaGasto) {

        return ResponseEntity.ok(
                categoriaGastoService.cambiarEstado(
                        idCategoriaGasto,
                        true));
    }

    @PatchMapping("/{idCategoriaGasto}/desactivar")
    public ResponseEntity<CategoriaGastoResponse> desactivar(
            @PathVariable Integer idCategoriaGasto) {

        return ResponseEntity.ok(
                categoriaGastoService.cambiarEstado(
                        idCategoriaGasto,
                        false));
    }
}