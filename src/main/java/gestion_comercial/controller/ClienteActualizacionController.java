package gestion_comercial.controller;

import gestion_comercial.dto.request.ClienteActualizacionRequest;
import gestion_comercial.dto.response.ClienteActualizacionResponse;
import gestion_comercial.service.interfaces.IClienteActualizacionService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/clientes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class ClienteActualizacionController {

    private final IClienteActualizacionService clienteActualizacionService;

    @PutMapping("/{idCliente}")
    public ResponseEntity<ClienteActualizacionResponse> actualizar(
            @PathVariable Integer idCliente,
            @Valid @RequestBody ClienteActualizacionRequest request
    ) {

        return ResponseEntity.ok(
                clienteActualizacionService.actualizar(
                        idCliente,
                        request
                )
        );
    }
}
