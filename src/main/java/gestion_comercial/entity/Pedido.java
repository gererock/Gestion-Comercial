package gestion_comercial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;


@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "id_pedido")
    private Integer idPedido;


    @Column(
            name = "numero_pedido",
            nullable = false,
            length = 30
    )
    private String numeroPedido;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_medio_pago",
            nullable = false
    )
    private MedioPago medioPago;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_venta",
            nullable = false,
            length = 20
    )
    private TipoVenta tipoVenta;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado_pedido",
            nullable = false,
            length = 30
    )
    private EstadoPedido estadoPedido;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado_pago",
            nullable = false,
            length = 20
    )
    private EstadoPago estadoPago;


    @Column(
            name = "descripcion_pago"
    )
    private String descripcionPago;


    @Column(
            name = "fecha_actualizacion",
            nullable = false
    )
    private LocalDateTime fechaActualizacion;


    protected Pedido() {
    }


    public Integer getIdPedido() {

        return idPedido;
    }


    public void setIdPedido(
            Integer idPedido
    ) {

        this.idPedido =
                idPedido;
    }


    public String getNumeroPedido() {

        return numeroPedido;
    }


    public void setNumeroPedido(
            String numeroPedido
    ) {

        this.numeroPedido =
                numeroPedido;
    }


    public MedioPago getMedioPago() {

        return medioPago;
    }


    public void setMedioPago(
            MedioPago medioPago
    ) {

        this.medioPago =
                medioPago;
    }


    public TipoVenta getTipoVenta() {

        return tipoVenta;
    }


    public void setTipoVenta(
            TipoVenta tipoVenta
    ) {

        this.tipoVenta =
                tipoVenta;
    }


    public EstadoPedido getEstadoPedido() {

        return estadoPedido;
    }


    public void setEstadoPedido(
            EstadoPedido estadoPedido
    ) {

        this.estadoPedido =
                estadoPedido;
    }


    public EstadoPago getEstadoPago() {

        return estadoPago;
    }


    public void setEstadoPago(
            EstadoPago estadoPago
    ) {

        this.estadoPago =
                estadoPago;
    }


    public String getDescripcionPago() {

        return descripcionPago;
    }


    public void setDescripcionPago(
            String descripcionPago
    ) {

        this.descripcionPago =
                descripcionPago;
    }


    public LocalDateTime getFechaActualizacion() {

        return fechaActualizacion;
    }


    public void setFechaActualizacion(
            LocalDateTime fechaActualizacion
    ) {

        this.fechaActualizacion =
                fechaActualizacion;
    }
}