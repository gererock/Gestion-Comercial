package gestion_comercial.controller;

import gestion_comercial.dto.request.NotaInternaClienteRequest;
import gestion_comercial.dto.response.NotaInternaClienteResponse;
import gestion_comercial.service.interfaces.INotaInternaClienteService;

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
public class NotaInternaClienteController {

    private final INotaInternaClienteService notaInternaClienteService;

    @PatchMapping("/{idCliente}/nota-interna")
    public ResponseEntity<NotaInternaClienteResponse> actualizar(
            @PathVariable Integer idCliente,
            @Valid @RequestBody NotaInternaClienteRequest request
    ) {
        return ResponseEntity.ok(
                notaInternaClienteService.actualizar(idCliente, request)
        );
    }
}
