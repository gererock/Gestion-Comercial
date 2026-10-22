package gestion_comercial.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;


public record ClienteCreacionRequest(

       @NotBlank(
        message = "El nombre es obligatorio"
)
        @Size(
        max = 100,
        message = "El nombre no puede superar los 100 caracteres")
        @Pattern(
        regexp = "^$|^[\\p{L} ]+$",
        message = "El nombre solo puede contener letras y espacios")
        String nombre,


        @Size(
                max = 100,
                message = "El apellido no puede superar los 100 caracteres"
        )
        @Pattern(
                regexp = "^$|^(?=.*\\p{L})[\\p{L} ]+$",
                message = "El apellido solo puede contener letras y espacios"
        )
        String apellido,


        @Pattern(
                regexp = "^$|^[0-9]{7,8}$",
                message = "El DNI debe contener entre 7 y 8 números"
        )
        String dni,


        @Pattern(
                regexp = "^$|^[0-9]{8,15}$",
                message = "El teléfono debe contener entre 8 y 15 números"
        )
        String telefono,


        @Email(
                message = "El correo electrónico no tiene un formato válido"
        )
        @Pattern(
                regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "El correo electrónico no tiene un formato válido"
        )
        @Size(
                max = 120,
                message = "El correo no puede superar los 120 caracteres"
        )
        String correo,


        @Size(
                max = 150,
                message = "La razón social no puede superar los 150 caracteres"
        )
        @Pattern(
                regexp = "^$|^(?=.*[\\p{L}\\p{N}])[\\p{L}\\p{N} .,&'()/\\-]+$",
                message = "La razón social no tiene un formato válido"
        )
        String razonSocial,


        @Pattern(
                regexp = "^$|^[0-9]{11}$",
                message = "El CUIT debe contener exactamente 11 números"
        )
        String cuit,


        @Pattern(
                regexp = "^$|^(Consumidor Final|Responsable Inscripto|Monotributista|Exento)$",
                message = "La condición frente al IVA no es válida"
        )
        String condicionIva,


        @Size(
                max = 200,
                message = "La dirección de facturación no puede superar los 200 caracteres"
        )
        @Pattern(
                regexp = "^$|^(?=.*[\\p{L}\\p{N}]).+$",
                message = "La dirección de facturación no tiene un formato válido"
        )
        String direccionFacturacion,


        @Size(
                max = 100,
                message = "La localidad no puede superar los 100 caracteres"
        )
        @Pattern(
                regexp = "^$|^(?=.*\\p{L})[\\p{L} ]+$",
                message = "La localidad no tiene un formato válido"
        )
        String localidadFacturacion,


        @Size(
                max = 100,
                message = "La provincia no puede superar los 100 caracteres"
        )
        @Pattern(
                regexp = "^$|^(?=.*\\p{L})[\\p{L} ]+$",
                message = "La provincia no tiene un formato válido"
        )
        String provinciaFacturacion,


        @DecimalMin(
                value = "0.00",
                message = "El descuento no puede ser menor a 0"
        )
        @DecimalMax(
                value = "100.00",
                message = "El descuento no puede ser mayor a 100"
        )
        @Digits(
                integer = 3,
                fraction = 2,
                message = "El descuento puede tener como máximo dos decimales"
        )
        BigDecimal descuentoPorcentaje,


        @Size(
                max = 1000,
                message = "La nota interna no puede superar los 1000 caracteres"
        )
        String notaInterna,


        Boolean estado

) {
}