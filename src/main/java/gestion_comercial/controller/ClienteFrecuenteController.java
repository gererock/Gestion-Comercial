package gestion_comercial.controller;

import gestion_comercial.dto.response.ClienteFrecuenteResponse;
import gestion_comercial.entity.OrdenClienteFrecuente;
import gestion_comercial.entity.PeriodoClienteFrecuente;
import gestion_comercial.service.interfaces.IClienteFrecuenteService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/admin/clientes-frecuentes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class ClienteFrecuenteController {


    private final IClienteFrecuenteService
            clienteFrecuenteService;


    @GetMapping
    public ResponseEntity<List<ClienteFrecuenteResponse>> consultar(

            @RequestParam(required = false)
            PeriodoClienteFrecuente periodo,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate desde,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate hasta,

            @RequestParam(required = false)
            OrdenClienteFrecuente orden,

            @RequestParam(required = false)
            String buscar

    ) {

        List<ClienteFrecuenteResponse> clientes =
                clienteFrecuenteService
                        .consultarClientesFrecuentes(
                                periodo,
                                desde,
                                hasta,
                                orden,
                                buscar
                        );


        return ResponseEntity.ok(
                clientes
        );
    }
}