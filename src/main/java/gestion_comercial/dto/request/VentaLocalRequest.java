package gestion_comercial.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
public record VentaLocalRequest(
 @NotEmpty @Size(max=100) List<@NotNull @Valid Item> items,
 @NotNull UUID claveOperacion,
 @Positive Integer idCliente
) {
 // Compatibilidad con los consumidores Java de US24 sin cliente.
 public VentaLocalRequest(List<Item> items, UUID claveOperacion) {
  this(items, claveOperacion, null);
 }
 public record Item(@NotNull @Positive Integer idProducto, @NotNull @Positive Integer cantidad) {}
}
