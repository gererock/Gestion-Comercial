
package gestion_comercial.controller;

import gestion_comercial.dto.request.ConfirmarPedidoRequest;
import gestion_comercial.dto.response.ConfirmarPedidoResponse;
import gestion_comercial.service.impl.PedidoConfirmacionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cliente/pedidos")
@PreAuthorize("hasRole('CLIENTE')")
public class PedidoConfirmacionController {

    private final PedidoConfirmacionService pedidoService;

    public PedidoConfirmacionController(
            PedidoConfirmacionService pedidoService
    ) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<ConfirmarPedidoResponse> confirmar(
            Authentication authentication,

            @Valid
            @RequestBody
            ConfirmarPedidoRequest request
    ) {

        ConfirmarPedidoResponse respuesta =
                pedidoService.confirmar(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }
}
