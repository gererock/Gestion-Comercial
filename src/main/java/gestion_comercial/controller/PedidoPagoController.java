package gestion_comercial.controller;

import gestion_comercial.dto.request.EstadoPagoPedidoRequest;
import gestion_comercial.dto.response.EstadoPagoPedidoResponse;

import gestion_comercial.service.interfaces.IPedidoPagoService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/vendedor/pedidos")
@PreAuthorize(
        "hasAnyRole('VENDEDOR', 'ADMINISTRADOR')"
)
public class PedidoPagoController {

    private final IPedidoPagoService
            pedidoPagoService;


    public PedidoPagoController(
            IPedidoPagoService pedidoPagoService
    ) {

        this.pedidoPagoService =
                pedidoPagoService;
    }


    // =========================================
    // CONSULTAR PAGO - US20
    // =========================================

    @GetMapping("/{idPedido}/pago")
    public ResponseEntity<EstadoPagoPedidoResponse>
    consultar(

            @PathVariable
            Integer idPedido
    ) {

        return ResponseEntity.ok(
                pedidoPagoService.consultar(
                        idPedido
                )
        );
    }


    // =========================================
    // ACTUALIZAR PAGO - US20
    // =========================================

    @PatchMapping("/{idPedido}/pago")
    public ResponseEntity<EstadoPagoPedidoResponse>
    actualizar(

            @PathVariable
            Integer idPedido,

            @Valid
            @RequestBody
            EstadoPagoPedidoRequest request
    ) {

        return ResponseEntity.ok(
                pedidoPagoService.actualizar(

                        idPedido,

                        request
                )
        );
    }
}