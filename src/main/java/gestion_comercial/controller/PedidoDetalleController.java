package gestion_comercial.controller;

import gestion_comercial.dto.response.PedidoDetalleResponse;
import gestion_comercial.service.interfaces.IPedidoDetalleService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendedor/pedidos")
@PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')")
@RequiredArgsConstructor
public class PedidoDetalleController {

    private final IPedidoDetalleService pedidoDetalleService;

    @GetMapping("/{idPedido}")
    public ResponseEntity<PedidoDetalleResponse> buscarDetalle(
            @PathVariable Integer idPedido
    ) {
        return ResponseEntity.ok(
                pedidoDetalleService.buscarDetalle(idPedido)
        );
    }
}
