package gestion_comercial.service.impl;

import gestion_comercial.controller.HistorialVentaController;
import gestion_comercial.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HistorialVentaContratoTest {
    @Test void historialVacioResponde200ConArrayVacioSinEliminarDatos()throws Exception{
        var pedidos=mock(PedidoRepository.class);
        when(pedidos.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class))).thenReturn(List.of());
        var service=new HistorialVentaService(pedidos,mock(DetallePedidoRepository.class),mock(ClienteRepository.class));
        MockMvcBuilders.standaloneSetup(new HistorialVentaController(service)).build()
            .perform(get("/api/admin/ventas")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    @Test void servicioDeclaraTransaccionesDeSoloLectura()throws Exception{
        assertTrue(HistorialVentaService.class.getMethod("listarHistorial",java.time.LocalDate.class,java.time.LocalDate.class,java.time.LocalDate.class,String.class,java.math.BigDecimal.class,java.math.BigDecimal.class,gestion_comercial.entity.TipoVenta.class).getAnnotation(Transactional.class).readOnly());
        assertTrue(HistorialVentaService.class.getMethod("buscarDetalle",Integer.class).getAnnotation(Transactional.class).readOnly());
    }
    @Test void controladorSoloExponeGet(){
        int endpoints=0;
        for(var method:HistorialVentaController.class.getDeclaredMethods()){
            assertNull(method.getAnnotation(PostMapping.class));assertNull(method.getAnnotation(PutMapping.class));
            assertNull(method.getAnnotation(PatchMapping.class));assertNull(method.getAnnotation(DeleteMapping.class));
            assertNull(method.getAnnotation(RequestMapping.class));
            if(method.isAnnotationPresent(GetMapping.class))endpoints++;
        }
        assertEquals(2,endpoints);
    }
}
