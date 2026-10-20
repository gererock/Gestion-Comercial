package gestion_comercial.controller;

import gestion_comercial.dto.request.ActualizarEstadoPedidoRequest;
import gestion_comercial.dto.response.PedidoEstadoResponse;
import gestion_comercial.service.interfaces.IPedidoEstadoService;

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
@RequestMapping("/api/vendedor/pedidos")
@PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')")
@RequiredArgsConstructor
public class PedidoEstadoController {

    private final IPedidoEstadoService pedidoEstadoService;

    @PatchMapping("/{idPedido}/estado")
    public ResponseEntity<PedidoEstadoResponse> actualizar(
            @PathVariable Integer idPedido,
            @Valid @RequestBody ActualizarEstadoPedidoRequest request
    ) {
        return ResponseEntity.ok(
                pedidoEstadoService.actualizar(idPedido, request)
        );
    }
}
