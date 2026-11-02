package gestion_comercial.service.impl;

import gestion_comercial.entity.*;
import gestion_comercial.exception.ConsultaGastoException;
import gestion_comercial.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaGastoServiceTest {
    @Mock GastoRepository gastos;
    @Mock CategoriaGastoRepository categorias;
    @Mock MedioPagoRepository medios;
    @InjectMocks ConsultaGastoService servicio;
    private static final LocalDate HOY = LocalDate.of(2026, 3, 1);
    private static final LocalDate DESDE = LocalDate.of(2026, 2, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 2, 28);

    @ParameterizedTest
    @CsvSource({"DIA,2026-03-01,2026-03-01", "SEMANA,2026-02-23,2026-03-01", "MES,2026-03-01,2026-03-31", "RANGO,2026-02-01,2026-02-28"})
    void resuelvePeriodos(PeriodoConsultaGasto periodo, LocalDate desde, LocalDate hasta) {
        try (var fechas = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            fechas.when(() -> LocalDate.now(ZoneId.of("America/Argentina/Cordoba"))).thenReturn(HOY);
            when(gastos.buscarPorFiltros(desde, hasta, null, null)).thenReturn(List.of());
            var r = servicio.consultar(periodo, DESDE, HASTA, null, null);
            assertEquals(desde, r.desde()); assertEquals(hasta, r.hasta());
            verify(gastos).buscarPorFiltros(desde, hasta, null, null);
        }
    }
    @ParameterizedTest @NullSource @EnumSource(value=PeriodoConsultaGasto.class, names="RANGO")
    void sinFechasUsaMesActual(PeriodoConsultaGasto periodo) {
        try (var fechas = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            fechas.when(() -> LocalDate.now(ZoneId.of("America/Argentina/Cordoba"))).thenReturn(HOY);
            var r = servicio.consultar(periodo, null, null, null, null);
            assertEquals(HOY, r.desde()); assertEquals(HOY.plusDays(30), r.hasta());
        }
    }
    @ParameterizedTest @CsvSource({"2026-02-01,", ",2026-02-28", "2026-02-01,2026-02-28"})
    void infiereRangoYPermiteLimitesAbiertos(LocalDate desde, LocalDate hasta) {
        servicio.consultar(null, desde, hasta, null, null);
        verify(gastos).buscarPorFiltros(desde, hasta, null, null);
    }
    @ParameterizedTest @CsvSource({"1,", ",2", "1,2"})
    void validaYTransmiteFiltros(Integer categoria, Integer medio) {
        if (categoria != null) when(categorias.findById(categoria)).thenReturn(Optional.of(new CategoriaGasto()));
        if (medio != null) when(medios.findById(medio)).thenReturn(Optional.of(new MedioPago()));
        servicio.consultar(PeriodoConsultaGasto.RANGO, DESDE, HASTA, categoria, medio);
        verify(gastos).buscarPorFiltros(DESDE, HASTA, categoria, medio);
    }
    @Test void sinGastosDevuelveListaVaciaYTotalCero() {
        var r = servicio.consultar(PeriodoConsultaGasto.RANGO, DESDE, HASTA, null, null);
        assertTrue(r.gastos().isEmpty()); assertEquals(0, r.cantidad()); assertEquals(BigDecimal.ZERO, r.total());
        verifyNoInteractions(categorias, medios);
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"   ", "Perez"})
    void convierteCamposYSumaMontosExactos(String apellido) {
        var usuario = new Usuario(); usuario.setIdUsuario(3); usuario.setNombre("Ana"); usuario.setApellido(apellido);
        var categoria = CategoriaGasto.builder().idCategoriaGasto(1).nombre("Servicios").build();
        var medio = new MedioPago(2, "Efectivo", true);
        var a = Gasto.builder().idGasto(10).fecha(DESDE).categoriaGasto(categoria).usuario(usuario)
                .medioPago(medio).descripcion("Luz").monto(new BigDecimal("100.10")).observaciones("Factura").build();
        var b = Gasto.builder().idGasto(11).fecha(HASTA).categoriaGasto(categoria).usuario(usuario)
                .descripcion("Agua").monto(new BigDecimal("20.25")).build();
        when(gastos.buscarPorFiltros(DESDE,HASTA,null,null)).thenReturn(List.of(a,b));
        var r = servicio.consultar(PeriodoConsultaGasto.RANGO,DESDE,HASTA,null,null);
        assertEquals(new BigDecimal("120.35"),r.total()); assertEquals(2,r.cantidad());
        var item=r.gastos().getFirst();
        assertAll(() -> assertEquals(10,item.idGasto()), () -> assertEquals(DESDE,item.fecha()),
                () -> assertEquals(1,item.idCategoriaGasto()), () -> assertEquals("Servicios",item.categoria()),
                () -> assertEquals("Luz",item.descripcion()), () -> assertEquals(a.getMonto(),item.monto()),
                () -> assertEquals(2,item.idMedioPago()), () -> assertEquals("Efectivo",item.medioPago()),
                () -> assertEquals(3,item.idUsuario()), () -> assertEquals("Factura",item.observaciones()),
                () -> assertEquals("Ana"+(apellido==null || apellido.isBlank()?"":" "+apellido),item.usuario()));
        assertNull(r.gastos().get(1).medioPago()); assertNull(r.gastos().get(1).idMedioPago());
    }
    @Test void rangoInvertidoSeRechazaAntesDeConsultar() {
        var e=assertThrows(ConsultaGastoException.class,()->servicio.consultar(PeriodoConsultaGasto.RANGO,HASTA,DESDE,null,null));
        assertEquals(HttpStatus.BAD_REQUEST,e.getStatus()); verifyNoInteractions(gastos,categorias,medios);
    }
    @ParameterizedTest @CsvSource({"0,", "-1,", ",0", ",-1", "99,", ",99"})
    void filtrosInvalidosOInexistentes(Integer categoria,Integer medio) {
        var e=assertThrows(ConsultaGastoException.class,()->servicio.consultar(PeriodoConsultaGasto.RANGO,DESDE,HASTA,categoria,medio));
        assertEquals((Objects.equals(categoria,99)||Objects.equals(medio,99))?HttpStatus.NOT_FOUND:HttpStatus.BAD_REQUEST,e.getStatus());
        verifyNoInteractions(gastos);
    }
}
