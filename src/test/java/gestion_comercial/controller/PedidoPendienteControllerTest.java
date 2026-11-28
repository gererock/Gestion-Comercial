package gestion_comercial.controller;

import gestion_comercial.dto.response.PedidoPendienteResponse;
import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.MedioEntrega;
import gestion_comercial.service.interfaces.IPedidoPendienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoPendienteControllerTest {

    @Mock IPedidoPendienteService servicio;

    private PedidoPendienteController controlador;

    @BeforeEach
    void preparar() {
        controlador = new PedidoPendienteController(servicio);
    }

    @Test
    void listadoPasaTodosLosParametrosAlServicioYRespondeConLaLista() {
        var esperado = List.of(new PedidoPendienteResponse(
                7, "US53-7", 9, "Ana Pérez", LocalDateTime.of(2026, 11, 27, 10, 0),
                EstadoPedido.CONFIRMADO, EstadoPago.PAGADO, MedioEntrega.ENVIO,
                null, "Calle 1", new BigDecimal("80.00")
        ));
        when(servicio.listarPendientes(
                "Ana", EstadoPedido.CONFIRMADO, MedioEntrega.ENVIO, EstadoPago.PAGADO
        )).thenReturn(esperado);

        ResponseEntity<List<PedidoPendienteResponse>> respuesta = controlador.listar(
                "Ana", EstadoPedido.CONFIRMADO, MedioEntrega.ENVIO, EstadoPago.PAGADO
        );

        assertEquals(200, respuesta.getStatusCode().value());
        assertSame(esperado, respuesta.getBody());
        verify(servicio).listarPendientes(
                "Ana", EstadoPedido.CONFIRMADO, MedioEntrega.ENVIO, EstadoPago.PAGADO
        );
    }

    @Test
    void detallePasaElIdAlServicioYDevuelveLaRespuesta() {
        var esperado = new PedidoPendienteResponse(
                7, "US53-7", 9, "Ana Pérez", LocalDateTime.of(2026, 11, 27, 10, 0),
                EstadoPedido.PENDIENTE_APROBACION, EstadoPago.PENDIENTE,
                MedioEntrega.RETIRO_LOCAL, null, null, new BigDecimal("80.00")
        );
        when(servicio.buscarPendiente(7)).thenReturn(esperado);

        var respuesta = controlador.buscar(7);

        assertEquals(200, respuesta.getStatusCode().value());
        assertSame(esperado, respuesta.getBody());
        verify(servicio).buscarPendiente(7);
    }

    @Test
    void controladorLimitaAccesoAAdministradorYVendedorYExponeSoloConsultasGet() {
        assertEquals("/api/vendedor/pedidos/pendientes",
                PedidoPendienteController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')",
                PedidoPendienteController.class.getAnnotation(PreAuthorize.class).value());

        var metodos = PedidoPendienteController.class.getDeclaredMethods();
        assertEquals(2, java.util.Arrays.stream(metodos)
                .filter(metodo -> metodo.isAnnotationPresent(GetMapping.class)).count());
        for (var metodo : metodos) {
            assertFalse(java.util.Arrays.stream(metodo.getAnnotations())
                    .anyMatch(anotacion -> anotacion.annotationType().getSimpleName().matches("(Post|Put|Patch|Delete)Mapping")));
        }
    }
}
