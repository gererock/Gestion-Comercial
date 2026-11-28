package gestion_comercial.service.impl;

import gestion_comercial.dto.response.PedidoPendienteResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.EstadoPago;
import gestion_comercial.entity.EstadoPedido;
import gestion_comercial.entity.LocalidadEnvio;
import gestion_comercial.entity.MedioEntrega;
import gestion_comercial.entity.Pedido;
import gestion_comercial.entity.TipoVenta;
import gestion_comercial.repository.PedidoRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PedidoPendienteServiceTest {

    private static final List<EstadoPedido> ESTADOS_PENDIENTES = List.of(
            EstadoPedido.PENDIENTE_APROBACION,
            EstadoPedido.CONFIRMADO,
            EstadoPedido.EN_PREPARACION,
            EstadoPedido.LISTO_PARA_RETIRAR,
            EstadoPedido.LISTO_PARA_ENVIAR,
            EstadoPedido.ENVIADO
    );

    @Mock PedidoRepository pedidos;
    @Mock Root<Pedido> root;
    @Mock CriteriaBuilder criteria;
    @Mock Path<TipoVenta> tipoVentaPath;
    @Mock Path<EstadoPedido> estadoPedidoPath;
    @Mock Path<MedioEntrega> medioEntregaPath;
    @Mock Path<EstadoPago> estadoPagoPath;
    @Mock Path<Object> clientePath;
    @Mock Predicate onlinePredicate;
    @Mock Predicate estadosPredicate;
    @Mock Predicate clientePredicate;
    @Mock Predicate estadoFiltroPredicate;
    @Mock Predicate entregaFiltroPredicate;
    @Mock Predicate pagoFiltroPredicate;
    @Mock Predicate combinacionPredicate;

    private PedidoPendienteService servicio;

    @BeforeEach
    void preparar() {
        servicio = new PedidoPendienteService(pedidos);
        when(root.<TipoVenta>get("tipoVenta")).thenReturn(tipoVentaPath);
        when(root.<EstadoPedido>get("estadoPedido")).thenReturn(estadoPedidoPath);
        when(root.<MedioEntrega>get("medioEntrega")).thenReturn(medioEntregaPath);
        when(root.<EstadoPago>get("estadoPago")).thenReturn(estadoPagoPath);
        when(root.get("cliente")).thenReturn(clientePath);
        when(criteria.equal(tipoVentaPath, TipoVenta.ONLINE)).thenReturn(onlinePredicate);
        when(estadoPedidoPath.in(
                EstadoPedido.PENDIENTE_APROBACION,
                EstadoPedido.CONFIRMADO,
                EstadoPedido.EN_PREPARACION,
                EstadoPedido.LISTO_PARA_RETIRAR,
                EstadoPedido.LISTO_PARA_ENVIAR,
                EstadoPedido.ENVIADO
        )).thenReturn(estadosPredicate);
        when(criteria.isNotNull(clientePath)).thenReturn(clientePredicate);
        when(criteria.equal(estadoPedidoPath, EstadoPedido.CONFIRMADO)).thenReturn(estadoFiltroPredicate);
        when(criteria.equal(estadoPedidoPath, EstadoPedido.ENTREGADO)).thenReturn(estadoFiltroPredicate);
        when(criteria.equal(estadoPedidoPath, EstadoPedido.CANCELADO)).thenReturn(estadoFiltroPredicate);
        when(criteria.equal(medioEntregaPath, MedioEntrega.ENVIO)).thenReturn(entregaFiltroPredicate);
        when(criteria.equal(medioEntregaPath, MedioEntrega.RETIRO_LOCAL)).thenReturn(entregaFiltroPredicate);
        when(criteria.equal(estadoPagoPath, EstadoPago.PAGADO)).thenReturn(pagoFiltroPredicate);
        when(criteria.equal(estadoPagoPath, EstadoPago.PENDIENTE)).thenReturn(pagoFiltroPredicate);
        when(criteria.and(any(Predicate[].class))).thenReturn(combinacionPredicate);
        when(pedidos.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());
    }

    @Test
    void especificacionRestringeOnlineYExactamenteLosSeisEstadosPendientes() {
        var consulta = capturarConsulta(null, null, null, null);

        assertEquals(List.of(onlinePredicate, estadosPredicate, clientePredicate),
                Arrays.asList(consulta.predicados()));
        assertEquals(6, ESTADOS_PENDIENTES.size());
        assertFalse(ESTADOS_PENDIENTES.contains(EstadoPedido.ENTREGADO));
        assertFalse(ESTADOS_PENDIENTES.contains(EstadoPedido.CANCELADO));
        verify(estadoPedidoPath).in(
                EstadoPedido.PENDIENTE_APROBACION,
                EstadoPedido.CONFIRMADO,
                EstadoPedido.EN_PREPARACION,
                EstadoPedido.LISTO_PARA_RETIRAR,
                EstadoPedido.LISTO_PARA_ENVIAR,
                EstadoPedido.ENVIADO
        );
        verify(criteria).equal(tipoVentaPath, TipoVenta.ONLINE);
        verify(criteria).isNotNull(clientePath);
    }

    @Test
    void busquedaCombinaNumeroNombreCompletoYRazonSocialEnLaCondicion() {
        @SuppressWarnings("unchecked") Join<Pedido, Cliente> clienteJoin = mock(Join.class);
        @SuppressWarnings("unchecked") Path<String> numeroPath = mock(Path.class);
        @SuppressWarnings("unchecked") Path<String> nombrePath = mock(Path.class);
        @SuppressWarnings("unchecked") Path<String> apellidoPath = mock(Path.class);
        @SuppressWarnings("unchecked") Path<String> razonSocialPath = mock(Path.class);
        @SuppressWarnings("unchecked") Expression<String> nombreCoalesce = mock(Expression.class);
        @SuppressWarnings("unchecked") Expression<String> apellidoCoalesce = mock(Expression.class);
        @SuppressWarnings("unchecked") Expression<String> razonSocialCoalesce = mock(Expression.class);
        @SuppressWarnings("unchecked") Expression<String> nombreParcial = mock(Expression.class);
        @SuppressWarnings("unchecked") Expression<String> nombreCompleto = mock(Expression.class);
        @SuppressWarnings("unchecked") Expression<String> numeroLower = mock(Expression.class);
        @SuppressWarnings("unchecked") Expression<String> nombreLower = mock(Expression.class);
        @SuppressWarnings("unchecked") Expression<String> razonSocialLower = mock(Expression.class);
        Predicate numeroMatch = mock(Predicate.class);
        Predicate nombreMatch = mock(Predicate.class);
        Predicate razonSocialMatch = mock(Predicate.class);
        Predicate busquedaPredicate = mock(Predicate.class);

        when(root.<String>get("numeroPedido")).thenReturn(numeroPath);
        when(root.join("cliente")).thenReturn((Join) clienteJoin);
        when(clienteJoin.<String>get("nombre")).thenReturn(nombrePath);
        when(clienteJoin.<String>get("apellido")).thenReturn(apellidoPath);
        when(clienteJoin.<String>get("razonSocial")).thenReturn(razonSocialPath);
        when(criteria.<String>coalesce(nombrePath, "")).thenReturn(nombreCoalesce);
        when(criteria.<String>coalesce(apellidoPath, "")).thenReturn(apellidoCoalesce);
        when(criteria.<String>coalesce(razonSocialPath, "")).thenReturn(razonSocialCoalesce);
        when(criteria.concat(nombreCoalesce, " ")).thenReturn(nombreParcial);
        when(criteria.concat(nombreParcial, apellidoCoalesce)).thenReturn(nombreCompleto);
        when(criteria.lower(numeroPath)).thenReturn(numeroLower);
        when(criteria.lower(nombreCompleto)).thenReturn(nombreLower);
        when(criteria.lower(razonSocialCoalesce)).thenReturn(razonSocialLower);
        when(criteria.like(numeroLower, "%us53-ab-42%")).thenReturn(numeroMatch);
        when(criteria.like(nombreLower, "%us53-ab-42%")).thenReturn(nombreMatch);
        when(criteria.like(razonSocialLower, "%us53-ab-42%")).thenReturn(razonSocialMatch);
        when(criteria.or(numeroMatch, nombreMatch, razonSocialMatch)).thenReturn(busquedaPredicate);
        when(criteria.and(any(Predicate[].class))).thenReturn(combinacionPredicate);

        var consulta = capturarConsulta("  US53-AB-42  ", null, null, null);

        verify(criteria).like(numeroLower, "%us53-ab-42%");
        verify(criteria).like(nombreLower, "%us53-ab-42%");
        verify(criteria).like(razonSocialLower, "%us53-ab-42%");
        verify(criteria).or(numeroMatch, nombreMatch, razonSocialMatch);
        assertTrue(Arrays.asList(consulta.predicados()).contains(busquedaPredicate));
    }

    @Test
    void filtroPorEstadoAgregaElEstadoSolicitadoAlaConsultaBase() {
        var consulta = capturarConsulta(null, EstadoPedido.CONFIRMADO, null, null);

        verify(criteria).equal(estadoPedidoPath, EstadoPedido.CONFIRMADO);
        assertTrue(Arrays.asList(consulta.predicados()).contains(estadoFiltroPredicate));
        assertTrue(Arrays.asList(consulta.predicados()).contains(estadosPredicate));
    }

    @Test
    void filtroPorModalidadAgregaLaCondicionDeEntrega() {
        var consulta = capturarConsulta(null, null, MedioEntrega.ENVIO, null);

        verify(criteria).equal(medioEntregaPath, MedioEntrega.ENVIO);
        assertTrue(Arrays.asList(consulta.predicados()).contains(entregaFiltroPredicate));
    }

    @Test
    void filtroPorPagoAgregaLaCondicionDePago() {
        var consulta = capturarConsulta(null, null, null, EstadoPago.PAGADO);

        verify(criteria).equal(estadoPagoPath, EstadoPago.PAGADO);
        assertTrue(Arrays.asList(consulta.predicados()).contains(pagoFiltroPredicate));
    }

    @Test
    void filtrosCombinadosConservanLaListaBase() {
        var consulta = capturarConsulta(null, EstadoPedido.CONFIRMADO, MedioEntrega.ENVIO, EstadoPago.PAGADO);
        verify(criteria).equal(estadoPedidoPath, EstadoPedido.CONFIRMADO);
        verify(criteria).equal(medioEntregaPath, MedioEntrega.ENVIO);
        verify(criteria).equal(estadoPagoPath, EstadoPago.PAGADO);
        assertTrue(Arrays.asList(consulta.predicados()).containsAll(List.of(
                onlinePredicate, estadosPredicate, estadoFiltroPredicate,
                entregaFiltroPredicate, pagoFiltroPredicate
        )));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"ENTREGADO", "CANCELADO"})
    void filtroDeEstadoCerradoSigueIntersectadoConLosSeisEstadosPendientes(EstadoPedido estadoCerrado) {
        var consulta = capturarConsulta(null, estadoCerrado, MedioEntrega.RETIRO_LOCAL, EstadoPago.PENDIENTE);

        verify(criteria).equal(estadoPedidoPath, estadoCerrado);
        assertTrue(Arrays.asList(consulta.predicados()).containsAll(List.of(
                estadosPredicate, estadoFiltroPredicate, entregaFiltroPredicate, pagoFiltroPredicate
        )));
    }

    @Test
    void ordenaPorFechaEIdDescendentes() {
        var consulta = capturarConsulta(null, null, null, null);

        assertEquals(List.of("fechaCreacion", "idPedido"),
                consulta.orden().stream().map(Sort.Order::getProperty).toList());
        assertTrue(consulta.orden().stream().allMatch(Sort.Order::isDescending));
    }

    @Test
    void buscarDetalleRestringePedidoOnlinePendienteDeAprobacion() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(31);
        cliente.setNombre("Ana");
        cliente.setApellido("Pérez");
        Pedido pedido = new Pedido();
        pedido.setIdPedido(42);
        pedido.setNumeroPedido("US53-42");
        pedido.setCliente(cliente);
        pedido.setTipoVenta(TipoVenta.ONLINE);
        pedido.setEstadoPedido(EstadoPedido.PENDIENTE_APROBACION);
        pedido.setEstadoPago(EstadoPago.PENDIENTE);
        pedido.setMedioEntrega(MedioEntrega.RETIRO_LOCAL);
        pedido.setFechaCreacion(LocalDateTime.of(2026, 11, 27, 10, 0));
        pedido.setTotal(new BigDecimal("125.50"));
        when(pedidos.findByIdPedidoAndTipoVentaAndEstadoPedidoAndClienteIsNotNull(
                42, TipoVenta.ONLINE, EstadoPedido.PENDIENTE_APROBACION
        )).thenReturn(Optional.of(pedido));

        PedidoPendienteResponse response = servicio.buscarPendiente(42);

        assertEquals("US53-42", response.numeroPedido());
        assertEquals("Ana Pérez", response.cliente());
        assertEquals(new BigDecimal("125.50"), response.total());
        verify(pedidos).findByIdPedidoAndTipoVentaAndEstadoPedidoAndClienteIsNotNull(
                42, TipoVenta.ONLINE, EstadoPedido.PENDIENTE_APROBACION
        );
    }

    private ConsultaCapturada capturarConsulta(
            String busqueda,
            EstadoPedido estadoPedido,
            MedioEntrega medioEntrega,
            EstadoPago estadoPago
    ) {
        servicio.listarPendientes(busqueda, estadoPedido, medioEntrega, estadoPago);
        @SuppressWarnings("unchecked") ArgumentCaptor<Specification<Pedido>> specificationCaptor =
                ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(pedidos).findAll(specificationCaptor.capture(), sortCaptor.capture());
        specificationCaptor.getValue().toPredicate(root, null, criteria);
        ArgumentCaptor<Predicate[]> predicatesCaptor = ArgumentCaptor.forClass(Predicate[].class);
        verify(criteria).and(predicatesCaptor.capture());
        return new ConsultaCapturada(predicatesCaptor.getValue(), sortCaptor.getValue());
    }

    private record ConsultaCapturada(Predicate[] predicados, Sort orden) { }
}
