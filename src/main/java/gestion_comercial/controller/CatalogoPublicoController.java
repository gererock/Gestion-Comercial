package gestion_comercial.controller;

import gestion_comercial.dto.response.CatalogoItemResponse;

import gestion_comercial.service.interfaces.ICatalogoPublicoService;

import gestion_comercial.dto.response.CatalogoFiltrosResponse;
import org.springframework.web.bind.annotation.RequestParam;

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
    listar(

            @RequestParam(required = false)
            String nombre,

            @RequestParam(required = false)
            Integer idCategoria,

            @RequestParam(required = false)
            Integer idMarca
    ) {

        return ResponseEntity.ok(
                catalogoPublicoService.listar(

                        nombre,

                        idCategoria,

                        idMarca
                )
        );
    }

    @GetMapping("/filtros")
    public ResponseEntity<CatalogoFiltrosResponse>
    obtenerFiltros() {

        return ResponseEntity.ok(
                catalogoPublicoService
                        .obtenerFiltros()
        );
    }
    }
    