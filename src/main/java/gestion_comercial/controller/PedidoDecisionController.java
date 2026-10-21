package gestion_comercial.controller;

import gestion_comercial.dto.response.PedidoDecisionResponse;
import gestion_comercial.service.interfaces.IPedidoDecisionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendedor/pedidos")
@PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')")
@RequiredArgsConstructor
public class PedidoDecisionController {

    private final IPedidoDecisionService pedidoDecisionService;

    @PatchMapping("/{idPedido}/aprobar")
    public ResponseEntity<PedidoDecisionResponse> aprobar(
            @PathVariable Integer idPedido,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                pedidoDecisionService.aprobar(
                        idPedido,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{idPedido}/cancelar")
    public ResponseEntity<PedidoDecisionResponse> cancelar(
            @PathVariable Integer idPedido,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                pedidoDecisionService.cancelar(
                        idPedido,
                        authentication.getName()
                )
        );
    }
}
