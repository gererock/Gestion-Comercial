package gestion_comercial.controller;

import gestion_comercial.dto.request.ImagenProductoOrdenRequest;
import gestion_comercial.dto.response.ImagenProductoResponse;

import gestion_comercial.service.interfaces.IImagenProductoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping(
        "/api/productos/{idProducto}/imagenes"
)
@PreAuthorize(
        "hasRole('ADMINISTRADOR')"
)
public class ImagenProductoController {

    private final IImagenProductoService
            imagenProductoService;


    public ImagenProductoController(
            IImagenProductoService imagenProductoService
    ) {

        this.imagenProductoService =
                imagenProductoService;
    }


    // =========================================
    // LISTAR
    // =========================================

    @GetMapping
    public ResponseEntity<List<ImagenProductoResponse>>
    listar(
            @PathVariable
            Integer idProducto
    ) {

        return ResponseEntity.ok(
                imagenProductoService.listar(
                        idProducto
                )
        );
    }


    // =========================================
    // AGREGAR
    // =========================================

    @PostMapping
    public ResponseEntity<ImagenProductoResponse>
    agregar(
            @PathVariable
            Integer idProducto,

            @RequestParam(
                    value = "archivo",
                    required = false
            )
            MultipartFile archivo,

            @RequestParam(
                    required = false
            )
            Integer orden,

            @RequestParam(
                    required = false,
                    defaultValue = "false"
            )
            Boolean principal
    ) {

        ImagenProductoResponse imagen =
                imagenProductoService.agregar(
                        idProducto,
                        archivo,
                        orden,
                        principal
                );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        imagen
                );
    }


    // =========================================
    // PRINCIPAL
    // =========================================

    @PatchMapping(
            "/{idImagen}/principal"
    )
    public ResponseEntity<ImagenProductoResponse>
    definirPrincipal(
            @PathVariable
            Integer idProducto,

            @PathVariable
            Integer idImagen
    ) {

        return ResponseEntity.ok(
                imagenProductoService
                        .definirPrincipal(
                                idProducto,
                                idImagen
                        )
        );
    }


    // =========================================
    // ORDEN
    // =========================================

    @PutMapping(
            "/{idImagen}/orden"
    )
    public ResponseEntity<ImagenProductoResponse>
    cambiarOrden(
            @PathVariable
            Integer idProducto,

            @PathVariable
            Integer idImagen,

            @Valid
            @RequestBody
            ImagenProductoOrdenRequest request
    ) {

        return ResponseEntity.ok(
                imagenProductoService
                        .cambiarOrden(
                                idProducto,
                                idImagen,
                                request
                        )
        );
    }


    // =========================================
    // ELIMINAR
    // =========================================

    @DeleteMapping(
            "/{idImagen}"
    )
    public ResponseEntity<Void> eliminar(
            @PathVariable
            Integer idProducto,

            @PathVariable
            Integer idImagen
    ) {

        imagenProductoService.eliminar(
                idProducto,
                idImagen
        );


        return ResponseEntity
                .noContent()
                .build();
    }
}