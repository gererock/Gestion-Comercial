package gestion_comercial.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gestion_comercial.dto.request.MarcaCreateRequest;
import gestion_comercial.dto.request.MarcaUpdateRequest;
import gestion_comercial.dto.request.MarcaCreateRequest;
import gestion_comercial.dto.response.MarcaResponse;
import gestion_comercial.service.interfaces.IMarcaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/marcas")
public class MarcaCategoria {

    private final IMarcaService marcaService;

    @PostMapping
    public ResponseEntity<MarcaResponse> crear(@Valid @RequestBody MarcaCreateRequest request) {
        MarcaResponse marcaCrear = marcaService.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(marcaCrear);
    }


    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody MarcaUpdateRequest request) {

            return ResponseEntity.ok(
                marcaService.actualizar(id, request)
            );
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<MarcaResponse> activar(@PathVariable Integer id) {
        return ResponseEntity.ok(marcaService.cambiarEstado(id, true));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<MarcaResponse> desactivar(@PathVariable Integer id) {
        return ResponseEntity.ok(marcaService.cambiarEstado(id, false));
    }

    @GetMapping 
    public ResponseEntity<List<MarcaResponse>> buscar(
        @RequestParam (required = false) String nombre,
        @RequestParam (required = false) Boolean activa
    ) {
        return ResponseEntity.ok(marcaService.buscar(nombre, activa));
    }
}
