package gestion_comercial.controller;

import gestion_comercial.dto.request.ReposicionStockRequest;
import gestion_comercial.dto.response.ReposicionStockResponse;
import gestion_comercial.service.interfaces.IReposicionStockService;

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
@RequestMapping("/api/admin/stock/reposiciones")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class ReposicionStockController {

    private final IReposicionStockService reposicionStockService;

    @PostMapping
    public ResponseEntity<ReposicionStockResponse> registrar(
            @Valid @RequestBody ReposicionStockRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reposicionStockService.registrar(
                        request,
                        authentication.getName()
                ));
    }
}
