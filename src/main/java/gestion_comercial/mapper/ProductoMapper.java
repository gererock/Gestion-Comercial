package gestion_comercial.mapper;

import gestion_comercial.dto.request.ProductoCreateRequest;
import gestion_comercial.dto.response.ProductoResponse;
import gestion_comercial.entity.Producto;

public class ProductoMapper {

    private ProductoMapper() {
    }


    public static Producto toEntity(
            ProductoCreateRequest request
    ) {

        Producto producto =
                new Producto();


        producto.setNombre(
                request.nombre()
        );


        producto.setDescripcion(
                request.descripcion()
        );


        producto.setPrecioCosto(
                request.precioCosto()
        );


        producto.setPrecioVenta(
                request.precioVenta()
        );


        producto.setStockActual(
                request.stockActual() != null
                        ? request.stockActual()
                        : 0
        );


        producto.setStockMinimo(
                request.stockMinimo() != null
                        ? request.stockMinimo()
                        : 0
        );


        producto.setCantidadMinimaMayorista(
                request.cantidadMinimaMayorista()
        );


        producto.setPorcentajeDescuentoMayorista(
                request.porcentajeDescuentoMayorista()
        );


        producto.setEnOferta(
                request.enOferta() != null
                        ? request.enOferta()
                        : false
        );


        producto.setPorcentajeDescuentoOferta(
                request.porcentajeDescuentoOferta()
        );


        producto.setEstado(
                request.estado()
        );


        producto.setPublicadoOnline(
                request.publicadoOnline() != null
                        ? request.publicadoOnline()
                        : true
        );


        return producto;
    }


    public static ProductoResponse toResponse(
            Producto producto
    ) {

        Integer idCategoria =
                producto.getCategoria() != null
                        ? producto.getCategoria()
                                .getId()
                        : null;


        Integer idMarca =
                producto.getMarca() != null
                        ? producto.getMarca()
                                .getId()
                        : null;


        Integer idGrupoCatalogo =
                producto.getGrupoCatalogo() != null
                        ? producto.getGrupoCatalogo()
                                .getIdGrupoCatalogo()
                        : null;


        return new ProductoResponse(

                producto.getIdProducto(),

                idCategoria,

                idMarca,

                idGrupoCatalogo,

                producto.getNombre(),

                producto.getDescripcion(),

                producto.getPrecioCosto(),

                producto.getPrecioVenta(),

                producto.getStockActual(),

                producto.getStockMinimo(),

                producto.getCantidadMinimaMayorista(),

                producto.getPorcentajeDescuentoMayorista(),

                producto.getEnOferta(),

                producto.getPorcentajeDescuentoOferta(),

                producto.getEstado(),

                producto.getPublicadoOnline(),

                producto.getFechaAlta()

        );
    }
}