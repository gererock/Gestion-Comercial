package gestion_comercial.dto.request;
import jakarta.validation.Valid;
import gestion_comercial.entity.EstadoPago;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
public record VentaLocalRequest(
 @NotEmpty @Size(max=100) List<@NotNull @Valid Item> items,
 @NotNull UUID claveOperacion,
 @Positive Integer idCliente,
 @NotNull @Positive Integer idMedioPago,
 @NotNull EstadoPago estadoPago,
 String descripcionPago
) {
 public record Item(@NotNull @Positive Integer idProducto, @NotNull @Positive Integer cantidad) {}
}
