package gestion_comercial.controller;

import gestion_comercial.dto.response.TicketPromedioResponse;
import gestion_comercial.entity.PeriodoTicketPromedio;
import gestion_comercial.entity.TipoTicketPromedio;
import gestion_comercial.service.interfaces.ITicketPromedioService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/admin/ticket-promedio")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class TicketPromedioController {


    private final ITicketPromedioService
            ticketPromedioService;


    @GetMapping
    public ResponseEntity<TicketPromedioResponse> consultar(

            @RequestParam(required = false)
            PeriodoTicketPromedio periodo,


            @RequestParam(required = false)
            TipoTicketPromedio tipo,


            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate desde,


            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate hasta
    ) {


        TicketPromedioResponse respuesta =
                ticketPromedioService
                        .consultarTicketPromedio(
                                periodo,
                                tipo,
                                desde,
                                hasta
                        );


        return ResponseEntity.ok(
                respuesta
        );
    }
}