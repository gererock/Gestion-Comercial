package gestion_comercial.service.interfaces;

import gestion_comercial.dto.request.VentaLocalRequest;
import gestion_comercial.dto.response.ProductoVentaLocalResponse;
import gestion_comercial.dto.response.VentaLocalResponse;
import java.util.List;

public interface IVentaLocalService {
    List<ProductoVentaLocalResponse> buscar(Integer idProducto, String nombre);
    VentaLocalResponse confirmar(String email, VentaLocalRequest request);
}
