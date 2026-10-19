package gestion_comercial.dto.request;

import gestion_comercial.entity.LocalidadEnvio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DireccionEnvioRequest(
    @NotBlank(message = "El nombre de la calle es obligatorio")
    @Size(max = 100, message = "El maximo de caracteres para ingresar el nombre de la calle es 100")
    String calle,

    @NotBlank(message = "El numero de la calle es obligatorio")
    @Size(max = 20, message = "El maximo de caracteres para ingresar el numero  es 20")
    String numero,

    @Size(max = 50, message = "El maximo de caracteres para ingresar el piso del departamento es 50")
    String pisoDepartamento,

    @NotNull(message = "La localidad es obligatoria")
    LocalidadEnvio localidad,

    String referencia
) {

}
