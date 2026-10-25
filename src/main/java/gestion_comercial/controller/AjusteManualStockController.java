package gestion_comercial.controller;

import gestion_comercial.dto.request.AjusteManualStockRequest;
import gestion_comercial.dto.response.AjusteManualStockResponse;
import gestion_comercial.service.interfaces.IAjusteManualStockService;

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
@RequestMapping("/api/admin/stock/ajustes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class AjusteManualStockController {

    private final IAjusteManualStockService ajusteManualStockService;

    @PostMapping
    public ResponseEntity<AjusteManualStockResponse> ajustar(
            @Valid @RequestBody AjusteManualStockRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ajusteManualStockService.ajustar(
                        request,
                        authentication.getName()
                ));
    }
}
