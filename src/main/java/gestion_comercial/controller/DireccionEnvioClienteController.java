package gestion_comercial.controller;

import gestion_comercial.dto.request.DireccionEnvioRequest;
import gestion_comercial.dto.response.DireccionEnvioResponse;

import gestion_comercial.service.interfaces.IDireccionEnvioService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/cliente/direcciones-envio")
@PreAuthorize("hasRole('CLIENTE')")
@RequiredArgsConstructor
public class DireccionEnvioClienteController {

    private final IDireccionEnvioService
            direccionEnvioService;

    @GetMapping
    public ResponseEntity<List<DireccionEnvioResponse>>
    listar(
            Authentication authentication
    ) {

        String emailUsuario =
                authentication.getName();


        return ResponseEntity.ok(
                direccionEnvioService.listar(
                        emailUsuario
                )
        );
    }


    @PostMapping
    public ResponseEntity<DireccionEnvioResponse>
    registrar(
            Authentication authentication,

            @Valid
            @RequestBody
            DireccionEnvioRequest request
    ) {

        String emailUsuario =
                authentication.getName();


        DireccionEnvioResponse direccion =
                direccionEnvioService.registrar(
                        emailUsuario,
                        request
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(direccion);
    }

    @GetMapping("/{idDireccionEnvio}")
    public ResponseEntity<DireccionEnvioResponse>
    buscarPropia(
            Authentication authentication,

            @PathVariable
            Integer idDireccionEnvio
    ) {

        String emailUsuario =
                authentication.getName();


        return ResponseEntity.ok(
                direccionEnvioService.buscarPropia(
                        emailUsuario,
                        idDireccionEnvio
                )
        );
    }
}