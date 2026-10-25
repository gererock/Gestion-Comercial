package gestion_comercial.service.impl;

import gestion_comercial.dto.response.ProductoStockBajoResponse;
import gestion_comercial.entity.EstadoProducto;
import gestion_comercial.entity.Producto;
import gestion_comercial.exception.ConsultaStockBajoException;
import gestion_comercial.repository.ProductoRepository;
import gestion_comercial.service.interfaces.IProductoStockBajoService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoStockBajoService implements IProductoStockBajoService {

    private static final int LONGITUD_MAXIMA_NOMBRE = 120;

    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoStockBajoResponse> consultar(
            String idProducto,
            String nombre,
            String idCategoria,
            String idMarca,
            String estado
    ) {
        Integer producto = convertirIdOpcional(
                idProducto,
                "El ID del producto no es válido"
        );
        String nombreNormalizado = convertirNombre(nombre);
        Integer categoria = convertirIdOpcional(
                idCategoria,
                "La categoría seleccionada no es válida"
        );
        Integer marca = convertirIdOpcional(
                idMarca,
                "La marca seleccionada no es válida"
        );
        EstadoProducto estadoProducto = convertirEstado(estado);

        return productoRepository
                .consultarProductosConStockBajo(
                        producto,
                        nombreNormalizado,
                        categoria,
                        marca,
                        estadoProducto
                )
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    private Integer convertirIdOpcional(
            String valor,
            String mensaje
    ) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }

        if (valor.isBlank() || !valor.matches("^[1-9][0-9]*$")) {
            throw error(mensaje);
        }

        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException exception) {
            throw error(mensaje);
        }
    }

    private String convertirNombre(String valor) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }

        if (valor.isBlank()) {
            throw error("El nombre de búsqueda no puede contener solo espacios");
        }

        String nombre = valor.trim();

        if (nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw error("El nombre de búsqueda no puede superar los 120 caracteres");
        }

        if (!nombre.matches(".*[\\p{L}\\p{N}].*")) {
            throw error("El nombre de búsqueda debe contener letras o números");
        }

        return nombre;
    }

    private EstadoProducto convertirEstado(String valor) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }

        if (valor.isBlank()) {
            throw error("El estado seleccionado no es válido");
        }

        try {
            return EstadoProducto.valueOf(valor);
        } catch (IllegalArgumentException exception) {
            throw error("El estado seleccionado no es válido");
        }
    }

    private ProductoStockBajoResponse convertirRespuesta(Producto producto) {
        Integer stockActual = producto.getStockActual();
        Integer stockMinimo = producto.getStockMinimo();

        if (stockActual == null || stockActual < 0) {
            throw new ConsultaStockBajoException(
                    HttpStatus.CONFLICT,
                    "Se detectó un producto con stock actual inválido"
            );
        }

        if (stockMinimo == null || stockMinimo < 0) {
            throw new ConsultaStockBajoException(
                    HttpStatus.CONFLICT,
                    "Se detectó un producto con stock mínimo inválido"
            );
        }

        if (stockActual > stockMinimo) {
            throw new ConsultaStockBajoException(
                    HttpStatus.CONFLICT,
                    "La consulta devolvió un producto que no posee stock bajo"
            );
        }

        return new ProductoStockBajoResponse(
                producto.getIdProducto(),
                producto.getNombre(),
                producto.getCategoria() == null
                        ? null
                        : producto.getCategoria().getId(),
                producto.getCategoria() == null
                        ? null
                        : producto.getCategoria().getNombre(),
                producto.getMarca() == null
                        ? null
                        : producto.getMarca().getId(),
                producto.getMarca() == null
                        ? null
                        : producto.getMarca().getNombre(),
                stockActual,
                stockMinimo,
                producto.getEstado(),
                producto.getPrecioVenta(),
                true,
                stockActual == 0 ? "SIN_STOCK" : "STOCK_BAJO"
        );
    }

    private ConsultaStockBajoException error(String mensaje) {
        return new ConsultaStockBajoException(
                HttpStatus.BAD_REQUEST,
                mensaje
        );
    }
}
