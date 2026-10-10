package gestion_comercial.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(
                max = 100,
                message = "El nombre no puede superar los 100 caracteres"
        )
        @Pattern(
                regexp = "^[\\p{L} ]+$",
                message = "El nombre solo puede contener letras"
        )
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(
                max = 100,
                message = "El apellido no puede superar los 100 caracteres"
        )
        @Pattern(
                regexp = "^[\\p{L} ]+$",
                message = "El apellido solo puede contener letras"
        )
        String apellido,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo electrónico no tiene un formato válido")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "El correo electrónico no tiene un formato válido"
        )
        @Size(
                max = 120,
                message = "El correo no puede superar los 120 caracteres"
        )
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(
                min = 8,
                message = "La contraseña debe tener al menos 8 caracteres"
        )
        String password,

        @NotBlank(message = "La confirmación de contraseña es obligatoria")
        String confirmacionPassword

) {
}