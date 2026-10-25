package gestion_comercial.controller;

import gestion_comercial.dto.response.ClienteConsultaResponse;

import gestion_comercial.service.interfaces.IClienteConsultaService;

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
        "/api/vendedor/clientes"
)
@PreAuthorize(
        "hasAnyRole('VENDEDOR', 'ADMINISTRADOR')"
)
@RequiredArgsConstructor
public class ClienteConsultaController {


    private final IClienteConsultaService
            clienteConsultaService;

    @GetMapping
    public ResponseEntity<
            List<ClienteConsultaResponse>
            >
    listar(

            @RequestParam(
                    required = false
            )
            String busqueda
    ) {

        return ResponseEntity.ok(
                clienteConsultaService
                        .listar(
                                busqueda
                        )
        );
    }


    @GetMapping("/{idCliente}")
    public ResponseEntity<
            ClienteConsultaResponse
            >
    buscarPorId(

            @PathVariable
            Integer idCliente
    ) {

        return ResponseEntity.ok(
                clienteConsultaService
                        .buscarPorId(
                                idCliente
                        )
        );
    }
}