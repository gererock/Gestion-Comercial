package gestion_comercial.mapper;

import gestion_comercial.dto.response.ProductoConsultaResponse;

import gestion_comercial.entity.EstadoStockProducto;
import gestion_comercial.entity.Producto;


public final class ProductoConsultaMapper {

    private ProductoConsultaMapper() {
    }


    public static ProductoConsultaResponse toResponse(
            Producto producto
    ) {

        Integer stockActual =
                producto.getStockActual();

        Integer stockMinimo =
                producto.getStockMinimo();


        boolean tieneStock =
                stockActual > 0;


        boolean bajoStock =
                stockActual < stockMinimo;


        EstadoStockProducto estadoStock =
                obtenerEstadoStock(
                        stockActual,
                        stockMinimo
                );


        Integer idCategoria =
                producto.getCategoria() != null
                        ? producto.getCategoria()
                                .getId()
                        : null;


        String categoria =
                producto.getCategoria() != null
                        ? producto.getCategoria()
                                .getNombre()
                        : null;


        Integer idMarca =
                producto.getMarca() != null
                        ? producto.getMarca()
                                .getId()
                        : null;


        String marca =
                producto.getMarca() != null
                        ? producto.getMarca()
                                .getNombre()
                        : null;


        return new ProductoConsultaResponse(

                producto.getIdProducto(),

                producto.getNombre(),

                idCategoria,

                categoria,

                idMarca,

                marca,

                producto.getPrecioVenta(),

                stockActual,

                stockMinimo,

                producto.getEstado(),

                tieneStock,

                bajoStock,

                estadoStock
        );
    }


    private static EstadoStockProducto obtenerEstadoStock(
            Integer stockActual,
            Integer stockMinimo
    ) {

        if (stockActual == 0) {

            return EstadoStockProducto
                    .SIN_STOCK;
        }


        if (
                stockActual
                        <
                stockMinimo
        ) {

            return EstadoStockProducto
                    .BAJO_STOCK;
        }


        if (
                stockActual
                        .equals(
                                stockMinimo
                        )
        ) {

            return EstadoStockProducto
                    .LIMITE_MINIMO;
        }


        return EstadoStockProducto
                .DISPONIBLE;
    }
}