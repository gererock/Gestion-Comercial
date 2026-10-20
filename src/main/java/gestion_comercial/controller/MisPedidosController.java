package gestion_comercial.controller;

import gestion_comercial.dto.response.PedidoClienteResumenResponse;
import gestion_comercial.service.interfaces.IPedidoClienteConsultaService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cliente/pedidos")
@PreAuthorize("hasRole('CLIENTE')")
@RequiredArgsConstructor
public class MisPedidosController {

    private final IPedidoClienteConsultaService pedidoClienteConsultaService;

    @GetMapping
    public ResponseEntity<List<PedidoClienteResumenResponse>> listar(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                pedidoClienteConsultaService.listar(authentication.getName())
        );
    }
}
