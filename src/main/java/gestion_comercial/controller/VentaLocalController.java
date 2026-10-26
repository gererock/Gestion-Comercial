package gestion_comercial.controller;
import gestion_comercial.dto.request.VentaLocalRequest;
import gestion_comercial.dto.response.*;
import gestion_comercial.service.interfaces.IVentaLocalService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController
@RequestMapping("/api/vendedor/ventas-locales")
@PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')")
public class VentaLocalController {
 private final IVentaLocalService servicio;
 public VentaLocalController(IVentaLocalService servicio) { this.servicio=servicio; }
 @GetMapping("/productos")
 public List<ProductoVentaLocalResponse> buscar(@RequestParam(required=false) Integer idProducto,
 @RequestParam(required=false) String nombre) { return servicio.buscar(idProducto,nombre); }
 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 public VentaLocalResponse confirmar(Authentication auth,@Valid @RequestBody VentaLocalRequest request) {
 return servicio.confirmar(auth.getName(),request);
 }
}
