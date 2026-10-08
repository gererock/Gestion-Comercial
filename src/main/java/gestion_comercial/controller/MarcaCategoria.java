package gestion_comercial.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<MarcaResponse> crear(@Valid @RequestBody  MarcaCreateRequest request) {
        MarcaResponse marcaCrear = marcaService.crear(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(marcaCrear);
    }

    @GetMapping 
    public ResponseEntity<List<MarcaResponse>> obtenerTodo() {
        return ResponseEntity.ok(marcaService.obtenerTodo());
    }
}
