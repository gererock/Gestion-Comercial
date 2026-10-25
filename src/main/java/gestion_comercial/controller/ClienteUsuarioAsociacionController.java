package gestion_comercial.controller;

import gestion_comercial.dto.request.ClienteUsuarioAsociacionRequest;
import gestion_comercial.dto.response.ClienteAsociacionResponse;
import gestion_comercial.dto.response.ClienteUsuarioAsociacionResponse;
import gestion_comercial.dto.response.UsuarioAsociacionResponse;
import gestion_comercial.service.interfaces.IClienteUsuarioAsociacionService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/clientes-usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class ClienteUsuarioAsociacionController {

    private final IClienteUsuarioAsociacionService asociacionService;

    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteAsociacionResponse>> buscarClientes(
            @RequestParam(required = false) String busqueda
    ) {
        return ResponseEntity.ok(
                asociacionService.buscarClientes(busqueda)
        );
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioAsociacionResponse>> buscarUsuarios(
            @RequestParam(required = false) String busqueda
    ) {
        return ResponseEntity.ok(
                asociacionService.buscarUsuarios(busqueda)
        );
    }

    @PostMapping
    public ResponseEntity<ClienteUsuarioAsociacionResponse> asociar(
            @Valid @RequestBody ClienteUsuarioAsociacionRequest request
    ) {
        return ResponseEntity.ok(
                asociacionService.asociar(request)
        );
    }
}
