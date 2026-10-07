package gestion_comercial.controller;

import gestion_comercial.dto.request.GrupoCatalogoCreateRequest;
import gestion_comercial.dto.request.GrupoCatalogoUpdateRequest;
import gestion_comercial.dto.response.GrupoCatalogoResponse;
import gestion_comercial.service.interfaces.IGrupoCatalogoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

@RestController
@RequestMapping("/api/admin/grupos-catalogo")
public class GrupoCatalogoController {

    private final IGrupoCatalogoService
            grupoCatalogoService;


    public GrupoCatalogoController(
            IGrupoCatalogoService grupoCatalogoService
    ) {
        this.grupoCatalogoService =
                grupoCatalogoService;
    }


    @PostMapping
    public ResponseEntity<GrupoCatalogoResponse> crear(
            @Valid
            @RequestBody
            GrupoCatalogoCreateRequest request
    ) {

        GrupoCatalogoResponse grupo =
                grupoCatalogoService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grupo);
    }


    @GetMapping
    public ResponseEntity<List<GrupoCatalogoResponse>>
    listar() {

        return ResponseEntity.ok(
                grupoCatalogoService.listar()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<GrupoCatalogoResponse>
    obtenerPorId(
            @PathVariable Integer id
    ) {

        return ResponseEntity.ok(
                grupoCatalogoService
                        .buscarPorId(id)
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<GrupoCatalogoResponse>
    actualizar(
            @PathVariable Integer id,

            @Valid
            @RequestBody
            GrupoCatalogoUpdateRequest request
    ) {

        return ResponseEntity.ok(
                grupoCatalogoService.actualizar(
                        id,
                        request
                )
        );
    }
    @PatchMapping("/{id}/activar")
        public ResponseEntity<GrupoCatalogoResponse>
        activar(
                @PathVariable Integer id
        ) {

        return ResponseEntity.ok(
                grupoCatalogoService.cambiarEstado(
                        id,
                        true
                )
        );
        }


        @PatchMapping("/{id}/desactivar")
        public ResponseEntity<GrupoCatalogoResponse>
        desactivar(
                @PathVariable Integer id
        ) {

        return ResponseEntity.ok(
                grupoCatalogoService.cambiarEstado(
                        id,
                        false
                )
        );
        }
}