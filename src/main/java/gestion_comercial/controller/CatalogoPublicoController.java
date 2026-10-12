package gestion_comercial.controller;

import gestion_comercial.dto.response.CatalogoItemResponse;

import gestion_comercial.service.interfaces.ICatalogoPublicoService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/public/catalogo")
public class CatalogoPublicoController {

    private final ICatalogoPublicoService
            catalogoPublicoService;


    public CatalogoPublicoController(
            ICatalogoPublicoService catalogoPublicoService
    ) {

        this.catalogoPublicoService =
                catalogoPublicoService;
    }


    @GetMapping
    public ResponseEntity<List<CatalogoItemResponse>>
    listar() {

        return ResponseEntity.ok(
                catalogoPublicoService.listar()
        );
    }
}