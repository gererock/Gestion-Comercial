package gestion_comercial.controller;

import gestion_comercial.dto.response.PedidoPendienteResponse;
import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.MedioEntrega;

import gestion_comercial.service.interfaces.IPedidoPendienteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(
        "/api/vendedor/pedidos/pendientes"
)
@PreAuthorize(
        "hasAnyRole('VENDEDOR', 'ADMINISTRADOR')"
)
@RequiredArgsConstructor
public class PedidoPendienteController {


    private final IPedidoPendienteService
            pedidoPendienteService;


    @GetMapping
    public ResponseEntity<
            List<PedidoPendienteResponse>
            >
    listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) EstadoPedido estadoPedido,
            @RequestParam(required = false) MedioEntrega medioEntrega,
            @RequestParam(required = false) EstadoPago estadoPago
    ) {

        return ResponseEntity.ok(
                pedidoPendienteService
                        .listarPendientes(
                                busqueda,
                                estadoPedido,
                                medioEntrega,
                                estadoPago
                        )
        );
    }


    @GetMapping("/{idPedido}")
    public ResponseEntity<
            PedidoPendienteResponse
            >
    buscar(

            @PathVariable
            Integer idPedido
    ) {

        return ResponseEntity.ok(
                pedidoPendienteService
                        .buscarPendiente(
                                idPedido
                        )
        );
    }
}
