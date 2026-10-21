package gestion_comercial.controller;

import gestion_comercial.dto.response.PedidoPendienteResponse;

import gestion_comercial.service.interfaces.IPedidoPendienteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    listar() {

        return ResponseEntity.ok(
                pedidoPendienteService
                        .listarPendientes()
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