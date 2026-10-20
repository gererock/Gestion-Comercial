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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas_stock")
@Getter
@Setter
@NoArgsConstructor
public class ReservaStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_stock")
    private Integer idReservaStock;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_detalle_pedido",
            nullable = false,
            unique = true
    )
    private DetallePedido detallePedido;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoReservaStock estado = EstadoReservaStock.ACTIVA;

    @Column(
            name = "fecha_reserva",
            nullable = false
    )
    private LocalDateTime fechaReserva;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @PrePersist
    private void asignarFechaReserva() {
        if (fechaReserva == null) {
            fechaReserva = LocalDateTime.now();
        }
    }
}