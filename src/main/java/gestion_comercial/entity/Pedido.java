
package gestion_comercial.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer idPedido;

    @Column(name = "numero_pedido", nullable = false, length = 30)
    private String numeroPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_medio_pago", nullable = false)
    private MedioPago medioPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_direccion_envio")
    private DireccionEnvioCliente direccionEnvioCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_venta", nullable = false)
    private TipoVenta tipoVenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pedido", nullable = false)
    private EstadoPedido estadoPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false)
    private EstadoPago estadoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_entrega", nullable = false)
    private MedioEntrega medioEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "localidad_envio")
    private LocalidadEnvio localidadEnvio;

    @Column(name = "direccion_envio", length = 200)
    private String direccionEnvio;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "descuento_cliente", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoCliente = BigDecimal.ZERO;

    @Column(name = "descuento_mayorista", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoMayorista = BigDecimal.ZERO;

    @Column(name = "descuento_oferta", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoOferta = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "descripcion_pago")
    private String descripcionPago;

    @Column(name = "observaciones_cliente")
    private String observacionesCliente;

    @Column(name = "observaciones_internas")
    private String observacionesInternas;

    @Column(name = "clave_confirmacion", unique = true)
    private UUID claveConfirmacion;

    public Pedido() {
    }
}
