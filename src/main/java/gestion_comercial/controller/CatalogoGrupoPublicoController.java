package gestion_comercial.controller;

import gestion_comercial.dto.response.CatalogoOpcionGrupoResponse;

import gestion_comercial.service.interfaces.ICatalogoGrupoPublicoService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/public/catalogo/grupos")
public class CatalogoGrupoPublicoController {

    private final ICatalogoGrupoPublicoService
            catalogoGrupoPublicoService;


    public CatalogoGrupoPublicoController(
            ICatalogoGrupoPublicoService catalogoGrupoPublicoService
    ) {

        this.catalogoGrupoPublicoService =
                catalogoGrupoPublicoService;
    }


    // =========================================
    // OPCIONES AGRUPADAS - US05
    // =========================================

    @GetMapping("/{idGrupoCatalogo}/opciones")
    public ResponseEntity<List<CatalogoOpcionGrupoResponse>>
    obtenerOpciones(

            @PathVariable
            Integer idGrupoCatalogo
    ) {

        return ResponseEntity.ok(
                catalogoGrupoPublicoService
                        .obtenerOpciones(
                                idGrupoCatalogo
                        )
        );
    }
}