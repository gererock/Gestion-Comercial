package gestion_comercial.entity;

import javax.annotation.processing.Generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "marcas")
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 
public class Marca {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca")
    private Integer id_marca;

    @Column(name = "nombre", length = 100) 
    private String nombre;
    @Column(name = "activa", nullable = false)
    private Boolean activa;
}
