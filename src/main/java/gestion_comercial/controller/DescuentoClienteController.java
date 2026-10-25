package gestion_comercial.controller;

import gestion_comercial.dto.request.DescuentoClienteRequest;
import gestion_comercial.dto.response.DescuentoClienteResponse;
import gestion_comercial.service.interfaces.IDescuentoClienteService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/clientes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class DescuentoClienteController {

    private final IDescuentoClienteService descuentoClienteService;

    @PatchMapping("/{idCliente}/descuento")
    public ResponseEntity<DescuentoClienteResponse> actualizar(
            @PathVariable Integer idCliente,
            @Valid @RequestBody DescuentoClienteRequest request
    ) {
        return ResponseEntity.ok(
                descuentoClienteService.actualizar(idCliente, request)
        );
    }
}
