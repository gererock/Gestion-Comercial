package gestion_comercial.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
public record VentaLocalRequest(
 @NotEmpty @Size(max=100) List<@NotNull @Valid Item> items,
 @NotNull UUID claveOperacion
) {
 public record Item(@NotNull @Positive Integer idProducto, @NotNull @Positive Integer cantidad) {}
}
