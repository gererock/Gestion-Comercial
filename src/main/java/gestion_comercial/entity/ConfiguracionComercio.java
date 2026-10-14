package gestion_comercial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "configuracion_comercio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionComercio {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "id_configuracion")
    private Integer idConfiguracion;


    @Column(
            name = "monto_minimo_envio",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal montoMinimoEnvio;


    @Column(
            name = "fecha_actualizacion",
            nullable = false
    )
    private LocalDateTime fechaActualizacion;
}