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

@Getter
@Setter
@NoArgsConstructor 
@Table(name = "direcciones_envio_cliente")
@Entity 
public class DireccionEnvioCliente {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion_envio")
    private Integer idDireccionEnvio;


    @ManyToOne(fetch = FetchType.LAZY, optional = false) 
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "calle",  nullable = false, length = 100)
    private String calle;

    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @Column(name = "piso_departamento", length = 50)
    private String pisoDepartamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "localidad", nullable = false, length = 30)
    private LocalidadEnvio localidad;

    @Column(name = "referencia")
    private String referencia;

}
