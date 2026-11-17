package gestion_comercial.service.impl;

import gestion_comercial.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.hibernate.SessionFactory;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Reutiliza las fixtures y regresiones US44, incluyendo limpieza por IDs propios. */
class HistorialVentaFiltrosHttpTest extends HistorialVentaHttpTest {
    { marca="US45-"+UUID.randomUUID().toString().substring(0,8); }
    @ParameterizedTest
    @CsvSource(delimiter='|', value={
        "fecha=2090-01-01|2", "fecha=2089-12-31|0", "desde=2090-01-07|1",
        "hasta=2090-01-01|2", "desde=2090-01-02&hasta=2090-01-03|2",
        "desde=2090-01-01&hasta=2090-01-01|2", "tipoVenta=ONLINE|1", "tipoVenta=LOCAL|7",
        "montoMinimo=220|8", "montoMinimo=220.01|0", "montoMaximo=220|8",
        "montoMaximo=219.99|0", "montoMinimo=200&montoMaximo=230|8",
        "montoMinimo=220&montoMaximo=220|8", "montoMinimo=0|8", "montoMaximo=0|0",
        "fecha=2090-01-01&tipoVenta=ONLINE|1", "fecha=2090-01-01&tipoVenta=LOCAL|1",
        "cliente=Ana&tipoVenta=ONLINE|1", "cliente=Ana&tipoVenta=LOCAL|0",
        "cliente=Perez&desde=2090-01-01&hasta=2090-01-01|1",
        "montoMinimo=220&tipoVenta=LOCAL|7",
        "desde=2090-01-01&hasta=2090-01-01&cliente=Ana&montoMinimo=220&montoMaximo=220&tipoVenta=ONLINE|1"
    })
    void filtrosYCombinaciones(String query,int cantidad)throws Exception {
        // Aísla el resultado a la ventana de fixtures; permite preexistentes fuera de ella.
        var request=get("/api/admin/ventas").header("Authorization",auth());
        if(!query.contains("fecha=") && !query.contains("desde=")) request.param("desde","2090-01-01");
        if(!query.contains("fecha=") && !query.contains("hasta=")) request.param("hasta","2090-01-07");
        for(String param:query.split("&")){var pair=param.split("=",2);request.param(pair[0],pair[1]);}
        mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(cantidad));
    }

    @ParameterizedTest
    @ValueSource(strings={"desde=2090-02-01&hasta=2090-01-01", "fecha=2090-01-01&desde=2090-01-01",
        "fecha=2090-01-01&hasta=2090-01-01", "fecha=05-11-2026", "desde=abc", "hasta=2026-02-30",
        "montoMinimo=-1", "montoMaximo=-0.01", "montoMinimo=2&montoMaximo=1", "montoMinimo=abc",
        "montoMaximo=abc", "tipoVenta=OTRO", "cliente=     ", "cliente=@@@@", "cliente=InexistenteUS45zz999"})
    void invalidos400(String query)throws Exception{
        var request=get("/api/admin/ventas").header("Authorization",auth());
        for(String param:query.split("&")){var pair=param.split("=",2);request.param(pair[0],pair[1]);}
        mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isNotEmpty());
    }

    @ParameterizedTest @ValueSource(strings={"Ana","Perez","45123456","us45.persona@test.local"})
    void busquedaClientePorTodosLosCampos(String busqueda)throws Exception{
        jdbc.update("update clientes set dni=?,correo=? where id_cliente=?","45123456","us45.persona@test.local",persona.getIdCliente());
        try{
            mvc.perform(get("/api/admin/ventas").param("cliente",busqueda).param("fecha","2090-01-01")
                .header("Authorization",auth())).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].idPedido").value(ids.getFirst()));
        }finally{jdbc.update("update clientes set dni=null,correo=null where id_cliente=?",persona.getIdCliente());}
    }

    @Test void clienteExistenteSinVentasYMultiplesClientes()throws Exception{
        var c=new Cliente();c.setNombre(marca+" sin ventas");c.setApellido("Perez");clientes.saveAndFlush(c);
        try{
            mvc.perform(get("/api/admin/ventas").param("cliente",marca+" sin ventas").header("Authorization",auth()))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
            jdbc.update("update clientes set apellido='Perez' where id_cliente=?",empresa.getIdCliente());
            mvc.perform(get("/api/admin/ventas").param("cliente","  Perez  ").param("fecha","2090-01-01").header("Authorization",auth()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        }finally{
            jdbc.update("update clientes set apellido=null where id_cliente=?",empresa.getIdCliente());clientes.deleteById(c.getIdCliente());
        }
    }

    @Test void limitesDeDiaYMontosDecimalesYCero()throws Exception{
        var fecha=jdbc.queryForObject("select fecha_creacion from pedidos where id_pedido=?",java.time.LocalDateTime.class,ids.get(1));
        try{
            jdbc.update("update pedidos set fecha_creacion=?,total=0 where id_pedido=?",java.time.LocalDate.of(2090,1,1).atStartOfDay(),ids.get(0));
            jdbc.update("update pedidos set fecha_creacion=?,total=123.45 where id_pedido=?",java.time.LocalDate.of(2090,1,1).atTime(23,59,59,999999000),ids.get(1));
            mvc.perform(get("/api/admin/ventas").param("fecha","2090-01-01").header("Authorization",auth()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
            for(String monto:List.of("0","123.45")){
                mvc.perform(get("/api/admin/ventas").param("fecha","2090-01-01").param("montoMinimo",monto).param("montoMaximo",monto).header("Authorization",auth()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
            }
        }finally{
            for(int i=0;i<2;i++)jdbc.update("update pedidos set fecha_creacion=?,total=220 where id_pedido=?",fecha,ids.get(i));
        }
    }

    @ParameterizedTest @ValueSource(strings={"desde","hasta"})
    void extremosIndependientes(String extremo)throws Exception{
        String comparacion=extremo.equals("desde")?">= timestamp '2090-01-01'":"< timestamp '2090-01-02'";
        int esperado=jdbc.queryForObject("select count(*) from pedidos where fecha_creacion "+comparacion,Integer.class);
        mvc.perform(get("/api/admin/ventas").param(extremo,"2090-01-01").header("Authorization",auth()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(esperado));
    }

    @Test void listadoCargaClienteConUsuarioSinConsultasAdicionales()throws Exception{
        jdbc.update("update clientes set id_usuario=? where id_cliente=?",users.get("CLIENTE").getIdUsuario(),persona.getIdCliente());
        try{
            var stats=emf.unwrap(SessionFactory.class).getStatistics();stats.clear();
            mvc.perform(get("/api/admin/ventas").param("fecha","2090-01-01").header("Authorization",auth())).andExpect(status().isOk());
            assertTrue(stats.getPrepareStatementCount()<=3,"SQL: "+stats.getPrepareStatementCount());
            stats.clear();
            mvc.perform(get("/api/admin/ventas").param("cliente","Ana").header("Authorization",auth())).andExpect(status().isOk());
            assertTrue(stats.getPrepareStatementCount()<=4,"SQL con cliente: "+stats.getPrepareStatementCount());
        }finally{jdbc.update("update clientes set id_usuario=null where id_cliente=?",persona.getIdCliente());}
    }

    @Test void filtrosSoloLecturaSinNMasUno()throws Exception{
        var antes=snapshot("pedidos");var items=snapshot("detalle_pedido");var cs=snapshot("clientes");
        var stats=emf.unwrap(SessionFactory.class).getStatistics();stats.clear();
        mvc.perform(get("/api/admin/ventas").param("tipoVenta","LOCAL").header("Authorization",auth()))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].cliente").value("Consumidor final"));
        assertTrue(stats.getPrepareStatementCount()<=3,"SQL: "+stats.getPrepareStatementCount());
        mvc.perform(get("/api/admin/ventas").param("cliente","Ana").param("montoMinimo","0").header("Authorization",auth())).andExpect(status().isOk());
        assertEquals(antes,snapshot("pedidos"));assertEquals(items,snapshot("detalle_pedido"));assertEquals(cs,snapshot("clientes"));
    }

    @ParameterizedTest @ValueSource(strings={"VENDEDOR","CLIENTE","SIN_JWT"})
    void seguridadConFiltros(String rol)throws Exception{
        var request=get("/api/admin/ventas").param("tipoVenta","LOCAL");
        if(!rol.equals("SIN_JWT"))request.header("Authorization","Bearer "+token(rol));
        mvc.perform(request).andExpect(rol.equals("SIN_JWT")?status().isUnauthorized():status().isForbidden());
    }
}
