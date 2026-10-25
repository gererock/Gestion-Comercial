package gestion_comercial.controller;

import gestion_comercial.dto.request.ClienteCreacionRequest;
import gestion_comercial.dto.response.ClienteCreacionResponse;

import gestion_comercial.service.interfaces.IClienteCreacionService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(
        "/api/admin/clientes"
)
@PreAuthorize(
        "hasRole('ADMINISTRADOR')"
)
@RequiredArgsConstructor
public class ClienteCreacionController {


    private final IClienteCreacionService
            clienteCreacionService;


    @PostMapping
    public ResponseEntity<ClienteCreacionResponse>
    crear(

            @Valid
            @RequestBody
            ClienteCreacionRequest request
    ) {

        ClienteCreacionResponse response =
                clienteCreacionService.crear(
                        request
                );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        response
                );
    }
}