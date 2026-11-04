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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Fixtures propias confirmadas para que también sean visibles desde HTTP TCP; limpieza por ID. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties="spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers=TestDatabaseGuard.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HistorialVentaHttpTest {
    @Autowired MockMvc mvc;
    @Autowired PedidoRepository pedidos;
    @Autowired DetallePedidoRepository detalles;
    @Autowired ClienteRepository clientes;
    @Autowired ProductoRepository productos;
    @Autowired MedioPagoRepository medios;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired JwtService jwt;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager tm;
    @Autowired EntityManagerFactory emf;
    @LocalServerPort int port;
    final List<Integer> ids=new ArrayList<>(), userIds=new ArrayList<>();
    final Map<String,Usuario> users=new HashMap<>();
    Cliente persona,empresa;
    Producto producto,otro;
    MedioPago efectivo;
    String marca="US44-"+UUID.randomUUID().toString().substring(0,8);
    static BigDecimal money(String n){return new BigDecimal(n);}

    @BeforeAll void preparar() {
        new TransactionTemplate(tm).executeWithoutResult(s->{
            TestDatabaseGuard.validarNombre(jdbc.queryForObject("select current_database()",String.class));
            for(String rol:List.of("ADMINISTRADOR","VENDEDOR","CLIENTE")) {
                var u=new Usuario();u.setNombre("QA US44");u.setApellido(rol);u.setEmail(marca+rol+"@test.local");
                u.setRol(roles.findByNombre(rol).orElseThrow());u.setEstado(EstadoUsuario.ACTIVO);
                u.setPasswordHash(encoder.encode("Us44Test123!"));u.setFechaAlta(LocalDateTime.now());
                usuarios.saveAndFlush(u);users.put(rol,u);userIds.add(u.getIdUsuario());
            }
            persona=new Cliente();persona.setNombre("Ana");persona.setApellido("Perez");clientes.saveAndFlush(persona);
            empresa=new Cliente();empresa.setNombre("Contacto");empresa.setRazonSocial("Empresa US44 SA");clientes.saveAndFlush(empresa);
            efectivo=medios.saveAndFlush(new MedioPago(null,marca+" Efectivo",true));
            producto=new Producto();producto.setNombre(marca+" A");producto.setPrecioVenta(money("100.00"));productos.saveAndFlush(producto);
            otro=new Producto();otro.setNombre(marca+" B");otro.setPrecioVenta(money("60.00"));productos.saveAndFlush(otro);
            int i=0;
            for(var estado:EstadoPedido.values()) {
                var p=new Pedido();p.setNumeroPedido(marca+"-"+i);p.setTipoVenta(i==0?TipoVenta.ONLINE:TipoVenta.LOCAL);
                p.setCliente(i==0?persona:i==1?empresa:null);p.setMedioPago(i==2?null:efectivo);
                p.setEstadoPedido(estado);p.setEstadoPago(i==1?EstadoPago.PAGADO:EstadoPago.PENDIENTE);
                p.setMedioEntrega(i==0?MedioEntrega.ENVIO:MedioEntrega.RETIRO_LOCAL);
                if(i==0){p.setLocalidadEnvio(LocalidadEnvio.VILLA_MARIA);p.setDireccionEnvio("Calle Test 44");}
                // Primeros dos pedidos empatan en fecha: debe desempatar id DESC.
                p.setFechaCreacion(LocalDateTime.of(2090,1,1,10,0).plusDays(Math.max(0,i-1)));
                p.setFechaActualizacion(p.getFechaCreacion());p.setSubtotal(money("250.00"));
                p.setDescuentoCliente(money("10.00"));p.setDescuentoMayorista(money("15.00"));p.setDescuentoOferta(money("5.00"));p.setTotal(money("220.00"));
                pedidos.saveAndFlush(p);ids.add(p.getIdPedido());
                detalle(p,producto,2,"100.00","10.00","190.00");detalle(p,otro,1,"60.00","0.00","60.00");i++;
            }
        });
    }
    void detalle(Pedido p,Producto pr,int cantidad,String precio,String descuento,String subtotal){
        var d=new DetallePedido();d.setPedido(p);d.setProducto(pr);d.setCantidad(cantidad);d.setPrecioUnitario(money(precio));
        d.setDescuentoAplicado(money(descuento));d.setSubtotal(money(subtotal));detalles.saveAndFlush(d);
    }
    @AfterAll void limpiar(){
        new TransactionTemplate(tm).executeWithoutResult(s->{
            TestDatabaseGuard.validarNombre(jdbc.queryForObject("select current_database()",String.class));
            for(int id:ids){jdbc.update("delete from detalle_pedido where id_pedido=?",id);jdbc.update("delete from pedidos where id_pedido=?",id);}
            productos.deleteById(producto.getIdProducto());productos.deleteById(otro.getIdProducto());
            clientes.deleteById(persona.getIdCliente());clientes.deleteById(empresa.getIdCliente());medios.deleteById(efectivo.getIdMedioPago());
            for(int id:userIds) usuarios.deleteById(id);
        });
    }
    String token(String rol){return jwt.generateToken(users.get(rol));}
    String auth(){return "Bearer "+token("ADMINISTRADOR");}
    String path(int index){return "/api/admin/ventas/"+ids.get(index);}
    @ParameterizedTest @ValueSource(strings={"","/detalle"})
    void sinJwt401(String detalle)throws Exception{mvc.perform(get(detalle.isEmpty()?"/api/admin/ventas":path(0))).andExpect(status().isUnauthorized());}
    @ParameterizedTest @CsvSource({"VENDEDOR,false","VENDEDOR,true","CLIENTE,false","CLIENTE,true"})
    void rolesNoAdministrativos403(String rol,boolean detalle)throws Exception{
        mvc.perform(get(detalle?path(0):"/api/admin/ventas").header("Authorization","Bearer "+token(rol))).andExpect(status().isForbidden());
    }
    @ParameterizedTest @ValueSource(strings={"","/detalle"})
    void jwtInvalido401(String detalle)throws Exception{mvc.perform(get(detalle.isEmpty()?"/api/admin/ventas":path(0)).header("Authorization","Bearer invalido")).andExpect(status().isUnauthorized());}
    @Test void historialCompletoOrdenadoYUnaSolaConsultaJPA()throws Exception{
        var stats=emf.unwrap(SessionFactory.class).getStatistics();stats.clear();
        mvc.perform(get("/api/admin/ventas").header("Authorization",auth())).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idPedido").value(ids.getLast()))
            .andExpect(jsonPath("$[0].numeroPedido").value(marca+"-7"))
            .andExpect(jsonPath("$[0].fecha").value("2090-01-07T10:00:00"))
            .andExpect(jsonPath("$[0].cliente").value("Consumidor final"))
            .andExpect(jsonPath("$[0].tipoVenta").value("LOCAL"))
            .andExpect(jsonPath("$[0].estadoPedido").value("CANCELADO"))
            .andExpect(jsonPath("$[0].estadoPago").value("PENDIENTE"))
            .andExpect(jsonPath("$[0].medioPago").value(efectivo.getNombre()))
            .andExpect(jsonPath("$[0].medioEntrega").value("RETIRO_LOCAL"))
            .andExpect(jsonPath("$[0].total").value(220.00))
            .andExpect(jsonPath("$[6].idPedido").value(ids.get(1)))
            .andExpect(jsonPath("$[6].cliente").value("Empresa US44 SA"))
            .andExpect(jsonPath("$[6].estadoPago").value("PAGADO"))
            .andExpect(jsonPath("$[7].idPedido").value(ids.getFirst()))
            .andExpect(jsonPath("$[7].cliente").value("Ana Perez"))
            .andExpect(jsonPath("$[7].tipoVenta").value("ONLINE"))
            .andExpect(jsonPath("$[7].medioEntrega").value("ENVIO"))
            .andExpect(jsonPath("$[5].medioPago").value("Sin medio de pago"));
        // Otra consulta de autenticación carga usuario/rol; no deben crecer con las ventas.
        assertTrue(stats.getPrepareStatementCount()<=3,"Consultas SQL inesperadas: "+stats.getPrepareStatementCount());
    }
    @ParameterizedTest @ValueSource(ints={0,1,2,3,4,5,6,7})
    void detalleCompletoParaTodosLosEstadosYTipos(int i)throws Exception{
        String row="$["+(7-i)+"]";
        mvc.perform(get("/api/admin/ventas").header("Authorization",auth())).andExpect(status().isOk())
            .andExpect(jsonPath(row+".idPedido").value(ids.get(i))).andExpect(jsonPath(row+".numeroPedido").value(marca+"-"+i))
            .andExpect(jsonPath(row+".fecha").value(LocalDateTime.of(2090,1,1,10,0).plusDays(Math.max(0,i-1)).toString()+":00"))
            .andExpect(jsonPath(row+".cliente").value(i==0?"Ana Perez":i==1?"Empresa US44 SA":"Consumidor final"))
            .andExpect(jsonPath(row+".tipoVenta").value(i==0?"ONLINE":"LOCAL"))
            .andExpect(jsonPath(row+".estadoPedido").value(EstadoPedido.values()[i].name()))
            .andExpect(jsonPath(row+".estadoPago").value(i==1?"PAGADO":"PENDIENTE"))
            .andExpect(jsonPath(row+".medioPago").value(i==2?"Sin medio de pago":efectivo.getNombre()))
            .andExpect(jsonPath(row+".medioEntrega").value(i==0?"ENVIO":"RETIRO_LOCAL"))
            .andExpect(jsonPath(row+".total").value(220.00));
        mvc.perform(get(path(i)).header("Authorization",auth())).andExpect(status().isOk())
            .andExpect(jsonPath("$.idPedido").value(ids.get(i))).andExpect(jsonPath("$.numeroPedido").value(marca+"-"+i))
            .andExpect(jsonPath("$.fecha").value(LocalDateTime.of(2090,1,1,10,0).plusDays(Math.max(0,i-1)).toString()+":00"))
            .andExpect(jsonPath("$.cliente").value(i==0?"Ana Perez":i==1?"Empresa US44 SA":"Consumidor final"))
            .andExpect(jsonPath("$.tipoVenta").value(i==0?"ONLINE":"LOCAL"))
            .andExpect(jsonPath("$.estadoPedido").value(EstadoPedido.values()[i].name()))
            .andExpect(jsonPath("$.estadoPago").value(i==1?"PAGADO":"PENDIENTE"))
            .andExpect(jsonPath("$.medioPago").value(i==2?"Sin medio de pago":efectivo.getNombre()))
            .andExpect(jsonPath("$.medioEntrega").value(i==0?"ENVIO":"RETIRO_LOCAL"))
            .andExpect(jsonPath("$.subtotal").value(250.00)).andExpect(jsonPath("$.descuentoCliente").value(10.00))
            .andExpect(jsonPath("$.descuentoMayorista").value(15.00)).andExpect(jsonPath("$.descuentoOferta").value(5.00))
            .andExpect(jsonPath("$.total").value(220.00)).andExpect(jsonPath("$.productos.length()").value(2))
            .andExpect(jsonPath("$.productos[0].idProducto").value(producto.getIdProducto()))
            .andExpect(jsonPath("$.productos[0].producto").value(producto.getNombre()))
            .andExpect(jsonPath("$.productos[0].cantidad").value(2)).andExpect(jsonPath("$.productos[0].precioUnitario").value(100.00))
            .andExpect(jsonPath("$.productos[0].descuentoAplicado").value(10.00)).andExpect(jsonPath("$.productos[0].subtotal").value(190.00))
            .andExpect(jsonPath("$.productos[1].idProducto").value(otro.getIdProducto()))
            .andExpect(jsonPath("$.productos[1].producto").value(otro.getNombre()))
            .andExpect(jsonPath("$.productos[1].cantidad").value(1)).andExpect(jsonPath("$.productos[1].precioUnitario").value(60.00))
            .andExpect(jsonPath("$.productos[1].descuentoAplicado").value(0.00)).andExpect(jsonPath("$.productos[1].subtotal").value(60.00));
    }
    @ParameterizedTest @ValueSource(strings={"99999999","0","-1"})
    void inexistentesEInvalidos404(String id)throws Exception{
        mvc.perform(get("/api/admin/ventas/"+id).header("Authorization",auth())).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("La venta indicada no existe"));
    }
    @Test void idNoNumerico400()throws Exception{mvc.perform(get("/api/admin/ventas/abc").header("Authorization",auth())).andExpect(status().isBadRequest());}
    List<Map<String,Object>> snapshot(String tabla){return jdbc.queryForList("select * from "+tabla+" order by 1");}
    @Test void consultasRepetidasNoModificanNingunaColumna()throws Exception{
        var antes=snapshot("pedidos");var items=snapshot("detalle_pedido");String lista=null,detalle=null;
        for(int i=0;i<3;i++){
            var l=mvc.perform(get("/api/admin/ventas").header("Authorization",auth())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            var d=mvc.perform(get(path(0)).header("Authorization",auth())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            if(i>0){assertEquals(lista,l);assertEquals(detalle,d);}lista=l;detalle=d;
        }
        assertEquals(antes,snapshot("pedidos"));assertEquals(items,snapshot("detalle_pedido"));
    }
    @Test void cambioPrecioActualNoCambiaPrecioHistorico()throws Exception{
        jdbc.update("update productos set precio_venta=999 where id_producto=?",producto.getIdProducto());
        try{mvc.perform(get(path(0)).header("Authorization",auth())).andExpect(status().isOk())
            .andExpect(jsonPath("$.productos[0].precioUnitario").value(100.00)).andExpect(jsonPath("$.productos[0].subtotal").value(190.00))
            .andExpect(jsonPath("$.total").value(220.00));}
        finally{jdbc.update("update productos set precio_venta=100 where id_producto=?",producto.getIdProducto());}
    }
    @Test void servidorHttpRealLoginYAmbosEndpointsPorRol()throws Exception{
        try(var client=HttpClient.newHttpClient()){
            for(String rol:List.of("ADMINISTRADOR","VENDEDOR","CLIENTE")){
                var login=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/auth/login"))
                    .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString("{\"email\":\""+users.get(rol).getEmail()+"\",\"password\":\"Us44Test123!\"}")).build();
                var response=client.send(login,HttpResponse.BodyHandlers.ofString());assertEquals(200,response.statusCode());
                // El token retornado por login se usa en las solicitudes TCP siguientes.
                var matcher=java.util.regex.Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(response.body());assertTrue(matcher.find(),"Login sin token");
                String bearer=matcher.group(1);
                for(String path:List.of("/api/admin/ventas",path(0),path(2))){
                    var req=HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).header("Authorization","Bearer "+bearer).GET().build();
                    var r=client.send(req,HttpResponse.BodyHandlers.ofString());assertEquals(rol.equals("ADMINISTRADOR")?200:403,r.statusCode());
                    if(rol.equals("ADMINISTRADOR")){assertTrue(r.body().contains("idPedido"));}
                }
            }
            for(String path:List.of("/api/admin/ventas",path(0)))assertEquals(401,client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).GET().build(),HttpResponse.BodyHandlers.ofString()).statusCode());
        }
    }
}
