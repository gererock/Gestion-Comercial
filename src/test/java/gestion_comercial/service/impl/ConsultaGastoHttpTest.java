package gestion_comercial.service.impl;

import gestion_comercial.entity.*;
import gestion_comercial.repository.*;
import gestion_comercial.security.JwtService;
import gestion_comercial.support.TestDatabaseGuard;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers=TestDatabaseGuard.class)
@Transactional
class ConsultaGastoHttpTest {
    @Autowired MockMvc mvc;
    @Autowired GastoRepository gastos;
    @Autowired CategoriaGastoRepository categorias;
    @Autowired MedioPagoRepository medios;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired JwtService jwt;
    @Autowired EntityManager em;
    CategoriaGasto categoria, otra;
    MedioPago efectivo, transferencia;
    Usuario admin;
    LocalDate desde=LocalDate.of(2090,1,10), hasta=LocalDate.of(2090,1,20);

    @BeforeEach void preparar() {
        categoria=categorias.saveAndFlush(CategoriaGasto.builder().nombre("US57 "+UUID.randomUUID()).build());
        otra=categorias.saveAndFlush(CategoriaGasto.builder().nombre("US57 "+UUID.randomUUID()).build());
        efectivo=medios.saveAndFlush(new MedioPago(null,"US57 "+UUID.randomUUID(),true));
        transferencia=medios.saveAndFlush(new MedioPago(null,"US57 "+UUID.randomUUID(),true));
        admin=usuario("ADMINISTRADOR");
        guardar(desde.minusDays(1),categoria,efectivo,"900.00");
        guardar(desde,categoria,efectivo,"10.10");
        guardar(hasta,categoria,transferencia,"20.25");
        guardar(hasta,otra,efectivo,"30.30");
        guardar(hasta,otra,null,"40.40");
        guardar(hasta.plusDays(1),categoria,efectivo,"800.00");
        em.flush(); em.clear();
    }
    Usuario usuario(String rol) {
        var u=new Usuario(); u.setNombre("Ana");u.setApellido("Perez");u.setEmail(UUID.randomUUID()+"@test.local");
        u.setRol(roles.findByNombre(rol).orElseThrow());u.setEstado(EstadoUsuario.ACTIVO);
        u.setPasswordHash("test-no-login");u.setFechaAlta(LocalDateTime.now());return usuarios.saveAndFlush(u);
    }
    Gasto guardar(LocalDate fecha,CategoriaGasto c,MedioPago m,String monto) {
        return gastos.saveAndFlush(Gasto.builder().fecha(fecha).categoriaGasto(c).medioPago(m).usuario(admin)
                .descripcion("Gasto US57").monto(new BigDecimal(monto)).claveOperacion(UUID.randomUUID().toString()).build());
    }
    MockHttpServletRequestBuilder rango() {
        return get("/api/admin/gastos").param("periodo","RANGO").param("desde",desde.toString()).param("hasta",hasta.toString());
    }
    MockHttpServletRequestBuilder autorizado(MockHttpServletRequestBuilder req) {
        return req.header("Authorization","Bearer "+jwt.generateToken(admin));
    }
    @Test void sinAutenticacionDevuelve401() throws Exception {mvc.perform(rango()).andExpect(status().isUnauthorized());}
    @Test void tokenInvalidoDevuelve401() throws Exception {mvc.perform(rango().header("Authorization","Bearer invalido")).andExpect(status().isUnauthorized());}
    @ParameterizedTest @ValueSource(strings={"VENDEDOR","CLIENTE"})
    void otrosRolesDevuelven403(String rol) throws Exception {
        mvc.perform(rango().header("Authorization","Bearer "+jwt.generateToken(usuario(rol)))).andExpect(status().isForbidden());
    }
    @Test void administradorObtieneGastosRegistradosYTotalConLimitesInclusivos() throws Exception {
        mvc.perform(autorizado(rango())).andExpect(status().isOk())
                .andExpect(jsonPath("$.desde").value(desde.toString())).andExpect(jsonPath("$.hasta").value(hasta.toString()))
                .andExpect(jsonPath("$.cantidad").value(4)).andExpect(jsonPath("$.total").value(101.05))
                .andExpect(jsonPath("$.gastos.length()").value(4))
                .andExpect(jsonPath("$.gastos[3].fecha").value(desde.toString()))
                .andExpect(jsonPath("$.gastos[3].categoria").value(categoria.getNombre()))
                .andExpect(jsonPath("$.gastos[3].descripcion").value("Gasto US57"))
                .andExpect(jsonPath("$.gastos[3].monto").value(10.10))
                .andExpect(jsonPath("$.gastos[3].medioPago").value(efectivo.getNombre()))
                .andExpect(jsonPath("$.gastos[3].usuario").value("Ana Perez"));
    }
    @ParameterizedTest @CsvSource({"true,false,2,30.35", "false,true,2,40.40", "true,true,1,10.10"})
    void filtrosIndividualesYCombinados(boolean porCategoria,boolean porMedio,int cantidad,double total) throws Exception {
        var req=rango();
        if(porCategoria) req.param("idCategoriaGasto",categoria.getIdCategoriaGasto().toString());
        if(porMedio) req.param("idMedioPago",efectivo.getIdMedioPago().toString());
        mvc.perform(autorizado(req)).andExpect(status().isOk()).andExpect(jsonPath("$.cantidad").value(cantidad))
                .andExpect(jsonPath("$.gastos.length()").value(cantidad)).andExpect(jsonPath("$.total").value(total));
    }
    @Test void sinResultados() throws Exception {
        mvc.perform(autorizado(get("/api/admin/gastos").param("desde","2091-01-01").param("hasta","2091-01-02")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.gastos").isEmpty())
                .andExpect(jsonPath("$.cantidad").value(0)).andExpect(jsonPath("$.total").value(0));
    }
    @ParameterizedTest @EnumSource(value=PeriodoConsultaGasto.class,names={"DIA","SEMANA","MES"})
    void periodosDelCalendarioIncluyenGastoDeHoy(PeriodoConsultaGasto periodo) throws Exception {
        var hoy=LocalDate.now(ZoneId.of("America/Argentina/Cordoba"));
        guardar(hoy,categoria,efectivo,"12.34");
        LocalDate inicio=switch(periodo) {case DIA -> hoy;case SEMANA -> hoy.minusDays(hoy.getDayOfWeek().getValue()-1);default -> hoy.withDayOfMonth(1);};
        LocalDate fin=switch(periodo) {case DIA -> hoy;case SEMANA -> inicio.plusDays(6);default -> hoy.withDayOfMonth(hoy.lengthOfMonth());};
        mvc.perform(autorizado(get("/api/admin/gastos").param("periodo",periodo.name()).param("idCategoriaGasto",categoria.getIdCategoriaGasto().toString())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.desde").value(inicio.toString()))
                .andExpect(jsonPath("$.hasta").value(fin.toString())).andExpect(jsonPath("$.cantidad").value(1))
                .andExpect(jsonPath("$.total").value(12.34));
    }
    @ParameterizedTest @CsvSource({"periodo,ANUAL", "desde,fecha", "hasta,2026-02-30", "idCategoriaGasto,abc", "idMedioPago,abc", "idCategoriaGasto,0", "idMedioPago,-1"})
    void parametrosInvalidos(String parametro,String valor) throws Exception {
        mvc.perform(autorizado(get("/api/admin/gastos").param(parametro,valor))).andExpect(status().isBadRequest());
    }
    @ParameterizedTest @ValueSource(strings={"idCategoriaGasto","idMedioPago"})
    void filtrosInexistentes(String parametro) throws Exception {
        mvc.perform(autorizado(rango().param(parametro,"2147483647"))).andExpect(status().isNotFound());
    }
    @Test void rangoInvertido() throws Exception {
        mvc.perform(autorizado(get("/api/admin/gastos").param("desde",hasta.toString()).param("hasta",desde.toString())))
                .andExpect(status().isBadRequest());
    }
    @Test void consultaPersonalizadaOrdenaYConservaMedioPagoNulo() {
        var r=gastos.buscarPorFiltros(desde,hasta,null,null);
        assertEquals(4,r.size());assertNull(r.getFirst().getMedioPago());
        assertEquals(desde,r.getLast().getFecha());
        assertTrue(r.get(0).getIdGasto()>r.get(1).getIdGasto());
    }
    @Test void consultaPersonalizadaAdmiteRangosAbiertos() {
        assertEquals(4,gastos.buscarPorFiltros(desde,null,categoria.getIdCategoriaGasto(),null).size()+gastos.buscarPorFiltros(null,desde.minusDays(1),categoria.getIdCategoriaGasto(),null).size());
        assertEquals(1,gastos.buscarPorFiltros(null,desde.minusDays(1),categoria.getIdCategoriaGasto(),null).size());
    }
}

