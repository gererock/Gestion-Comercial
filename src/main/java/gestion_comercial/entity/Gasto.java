package gestion_comercial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "gastos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gasto {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "id_gasto")
    private Integer idGasto;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_categoria_gasto",
            nullable = false
    )
    private CategoriaGasto categoriaGasto;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "id_usuario",
            nullable = false
    )
    private Usuario usuario;


    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "id_medio_pago"
    )
    private MedioPago medioPago;


    @Column(
            name = "descripcion",
            nullable = false
    )
    private String descripcion;


    @Column(
            name = "monto",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal monto;


    @Column(
            name = "fecha",
            nullable = false
    )
    private LocalDate fecha;


    @Column(
            name = "observaciones"
    )
    private String observaciones;


    @Column(
            name = "clave_operacion",
            nullable = false,
            unique = true,
            length = 50
    )
    private String claveOperacion;
}