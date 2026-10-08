package gestion_comercial.dto.request;

import gestion_comercial.entity.EstadoProducto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoCreateRequest(

        @NotBlank(
                message = "El nombre del producto es obligatorio"
        )
        @Size(
                max = 120,
                message = "El nombre del producto no puede superar los 120 caracteres"
        )
        @Pattern(
                regexp = "(?s).*\\p{L}.*",
                message = "El nombre del producto debe contener al menos una letra"
        )
        String nombre,


        @Size(
                max = 500,
                message = "La descripción no puede superar los 500 caracteres"
        )
        String descripcion,


        Integer idCategoria,

        Integer idMarca,

        Integer idGrupoCatalogo,


        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "El precio de costo debe ser mayor o igual a cero"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "El precio de costo debe tener como máximo 10 enteros y 2 decimales"
        )
        BigDecimal precioCosto,


        @NotNull(
                message = "El precio de venta es obligatorio"
        )
        @DecimalMin(
                value = "0.01",
                inclusive = true,
                message = "El precio de venta debe ser mayor a cero"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "El precio de venta debe tener como máximo 10 enteros y 2 decimales"
        )
        BigDecimal precioVenta,


        @PositiveOrZero(
                message = "El stock actual no puede ser negativo"
        )
        Integer stockActual,


        @PositiveOrZero(
                message = "El stock mínimo no puede ser negativo"
        )
        Integer stockMinimo,


        @Positive(
                message = "La cantidad mínima mayorista debe ser mayor a cero"
        )
        Integer cantidadMinimaMayorista,


        @DecimalMin(
                value = "0.01",
                inclusive = true,
                message = "El porcentaje de descuento mayorista debe ser mayor a cero"
        )
        @DecimalMax(
                value = "100.00",
                inclusive = true,
                message = "El porcentaje de descuento mayorista no puede superar el 100%"
        )
        @Digits(
                integer = 3,
                fraction = 2,
                message = "El porcentaje de descuento mayorista debe tener como máximo 2 decimales"
        )
        BigDecimal porcentajeDescuentoMayorista,


        Boolean enOferta,


        @DecimalMin(
                value = "0.01",
                inclusive = true,
                message = "El porcentaje de oferta debe ser mayor a cero"
        )
        @DecimalMax(
                value = "100.00",
                inclusive = true,
                message = "El porcentaje de oferta no puede superar el 100%"
        )
        @Digits(
                integer = 3,
                fraction = 2,
                message = "El porcentaje de oferta debe tener como máximo 2 decimales"
        )
        BigDecimal porcentajeDescuentoOferta,


        @NotNull(
                message = "El estado del producto es obligatorio"
        )
        EstadoProducto estado,


        Boolean publicadoOnline

) {
}