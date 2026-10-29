package gestion_comercial.controller;

import gestion_comercial.dto.request.GastoCreateRequest;
import gestion_comercial.dto.response.GastoResponse;
import gestion_comercial.service.interfaces.IGastoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin/gastos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class GastoController {

    private final IGastoService gastoService;


    @PostMapping
    public ResponseEntity<GastoResponse> crear(
            @Valid
            @RequestBody
            GastoCreateRequest request,
            Authentication authentication
    ) {

        GastoResponse gasto =
                gastoService.crear(
                        request,
                        authentication.getName()
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gasto);
    }
}