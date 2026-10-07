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

import gestion_comercial.dto.request.CategoriaCreateRequest;
import gestion_comercial.dto.request.CategoriaUpdateRequest;
import gestion_comercial.dto.response.CategoriaResponse;
import gestion_comercial.service.interfaces.ICategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {
    private final ICategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaCreateRequest request) {
        CategoriaResponse categoria = categoriaService.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtenerPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(@PathVariable Integer id,
            @Valid @RequestBody CategoriaUpdateRequest request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<CategoriaResponse> activar(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                categoriaService.cambiarEstado(id, true));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<CategoriaResponse> desactivar(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                categoriaService.cambiarEstado(id, false));
    }

    @GetMapping 
    public ResponseEntity<List<CategoriaResponse>> buscar(
        @RequestParam(required = false) String nombre,
        @RequestParam(required = false) Boolean activa
    ) {
        return  ResponseEntity.ok(categoriaService.buscar(nombre, activa));
    }

}
