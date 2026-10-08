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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca")
    private Marca marca;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo_catalogo")
    private GrupoCatalogo grupoCatalogo;


    @Column(
            name = "nombre",
            nullable = false,
            length = 120
    )
    private String nombre;


    @Column(name = "descripcion")
    private String descripcion;


    @Column(
            name = "precio_costo",
            precision = 12,
            scale = 2
    )
    private BigDecimal precioCosto;


    @Column(
            name = "precio_venta",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal precioVenta;


    @Column(
            name = "stock_actual",
            nullable = false
    )
    private Integer stockActual = 0;


    @Column(
            name = "stock_minimo",
            nullable = false
    )
    private Integer stockMinimo = 0;


    @Column(name = "cantidad_minima_mayorista")
    private Integer cantidadMinimaMayorista;


    @Column(
            name = "porcentaje_descuento_mayorista",
            precision = 5,
            scale = 2
    )
    private BigDecimal porcentajeDescuentoMayorista;


    @Column(
            name = "en_oferta",
            nullable = false
    )
    private Boolean enOferta = false;


    @Column(
            name = "porcentaje_descuento_oferta",
            precision = 5,
            scale = 2
    )
    private BigDecimal porcentajeDescuentoOferta;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoProducto estado =
            EstadoProducto.ACTIVO;


    @Column(
            name = "publicado_online",
            nullable = false
    )
    private Boolean publicadoOnline = true;


    @Column(
            name = "fecha_alta",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime fechaAlta;
}