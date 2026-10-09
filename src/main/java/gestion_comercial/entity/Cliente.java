package gestion_comercial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @OneToOne
    @JoinColumn(name = "id_usuario", unique = true)
    private Usuario usuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", length = 100)
    private String apellido;

    @Column(name = "dni", length = 20)
    private String dni;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "correo", length = 120)
    private String correo;

    @Column(name = "razon_social", length = 150)
    private String razonSocial;

    @Column(name = "cuit", length = 20)
    private String cuit;

    @Column(name = "condicion_iva", length = 80)
    private String condicionIva;

    @Column(name = "direccion_facturacion", length = 200)
    private String direccionFacturacion;

    @Column(name = "localidad_facturacion", length = 100)
    private String localidadFacturacion;

    @Column(name = "provincia_facturacion", length = 100)
    private String provinciaFacturacion;

    @Column(name = "descuento_porcentaje", precision = 5, scale = 2)
    private BigDecimal descuentoPorcentaje;

    @Column(name = "nota_interna")
    private String notaInterna;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @Column(name = "fecha_alta", nullable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @PrePersist
    private void asignarFechaAlta() {

        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
    }
}