package gestion_comercial.controller;

import gestion_comercial.dto.response.ClienteHistorialCompraDetalleResponse;
import gestion_comercial.dto.response.ClienteHistorialCompraResumenResponse;
import gestion_comercial.service.interfaces.IClienteHistorialCompraService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vendedor/clientes/{idCliente}/historial")
@PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')")
@RequiredArgsConstructor
public class ClienteHistorialCompraController {

    private final IClienteHistorialCompraService clienteHistorialCompraService;

    @GetMapping
    public ResponseEntity<List<ClienteHistorialCompraResumenResponse>> listar(
            @PathVariable Integer idCliente
    ) {

        return ResponseEntity.ok(
                clienteHistorialCompraService.listar(idCliente)
        );
    }

    @GetMapping("/{idPedido}")
    public ResponseEntity<ClienteHistorialCompraDetalleResponse> buscarDetalle(
            @PathVariable Integer idCliente,
            @PathVariable Integer idPedido
    ) {

        return ResponseEntity.ok(
                clienteHistorialCompraService.buscarDetalle(
                        idCliente,
                        idPedido
                )
        );
    }
}