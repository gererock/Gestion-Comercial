package gestion_comercial.service.impl;

import gestion_comercial.entity.TipoVenta;
import gestion_comercial.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistorialVentaFiltrosServiceTest {
    PedidoRepository pedidos=mock(PedidoRepository.class);
    DetallePedidoRepository detalles=mock(DetallePedidoRepository.class);
    ClienteRepository clientes=mock(ClienteRepository.class);
    HistorialVentaService service=new HistorialVentaService(pedidos,detalles,clientes);

    @ParameterizedTest
    @CsvSource(delimiter='|',nullValues="NULL",value={
        "2026-11-05|2026-11-05|NULL|NULL|NULL|NULL",
        "2026-11-05|NULL|2026-11-05|NULL|NULL|NULL",
        "NULL|2026-11-06|2026-11-05|NULL|NULL|NULL",
        "NULL|NULL|NULL|NULL|-1|NULL", "NULL|NULL|NULL|NULL|NULL|-1",
        "NULL|NULL|NULL|NULL|2|1", "NULL|NULL|NULL|'   '|NULL|NULL",
        "NULL|NULL|NULL|@@@@|NULL|NULL"
    })
    void validaAntesDeConsultar(LocalDate fecha,LocalDate desde,LocalDate hasta,String cliente,BigDecimal min,BigDecimal max){
        assertThrows(IllegalArgumentException.class,()->service.listarHistorial(fecha,desde,hasta,cliente,min,max,null));
        verifyNoInteractions(pedidos,detalles,clientes);
    }

    @Test void clienteInexistenteNoConsultaPedidos(){
        when(clientes.buscarClientes("Inexistente")).thenReturn(List.of());
        var error=assertThrows(IllegalArgumentException.class,()->service.listarHistorial(null,null,null," Inexistente ",null,null,null));
        assertEquals("No existe ningún cliente que coincida con la búsqueda",error.getMessage());
        verifyNoInteractions(pedidos,detalles);
    }
}
