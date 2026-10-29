package gestion_comercial.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;


public record GastoCreateRequest(

        @NotNull(
                message = "La categoría de gasto es obligatoria"
        )
        @Positive(
                message = "La categoría de gasto no es válida"
        )
        Integer idCategoriaGasto,


        @Positive(
                message = "El medio de pago no es válido"
        )
        Integer idMedioPago,


        @NotBlank(
                message = "La descripción es obligatoria"
        )
        @Size(
                max = 500,
                message = "La descripción no puede superar los 500 caracteres"
        )
        @Pattern(
                regexp = "(?s)^(?=.*[\\p{L}\\p{N}]).+$",
                message = "La descripción debe contener al menos una letra o un número"
        )
        String descripcion,


        @NotNull(
                message = "El monto es obligatorio"
        )
        @DecimalMin(
                value = "0.01",
                message = "El monto debe ser mayor a cero"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "El monto debe tener como máximo 10 enteros y 2 decimales"
        )
        BigDecimal monto,


        @NotNull(
                message = "La fecha es obligatoria"
        )
        @PastOrPresent(
                message = "La fecha no puede ser futura"
        )
        LocalDate fecha,


        @Size(
                max = 1000,
                message = "Las observaciones no pueden superar los 1000 caracteres"
        )
        String observaciones,


        @NotBlank(
                message = "La identificación de la operación es obligatoria"
        )
        @Size(
                max = 50,
                message = "La identificación de la operación no es válida"
        )
        String claveOperacion
) {
}