package gestion_comercial.service.impl;

import gestion_comercial.dto.response.CatalogoItemResponse;

import gestion_comercial.entity.EstadoProducto;
import gestion_comercial.entity.GrupoCatalogo;
import gestion_comercial.entity.ImagenProducto;
import gestion_comercial.entity.Producto;
import gestion_comercial.entity.TipoItemCatalogo;

import gestion_comercial.repository.ImagenProductoRepository;
import gestion_comercial.repository.ProductoRepository;

import gestion_comercial.service.interfaces.ICatalogoPublicoService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class CatalogoPublicoService
        implements ICatalogoPublicoService {

    private final ProductoRepository
            productoRepository;

    private final ImagenProductoRepository
            imagenProductoRepository;


    public CatalogoPublicoService(
            ProductoRepository productoRepository,
            ImagenProductoRepository imagenProductoRepository
    ) {

        this.productoRepository =
                productoRepository;

        this.imagenProductoRepository =
                imagenProductoRepository;
    }


    // =========================================
    // LISTAR CATÁLOGO PÚBLICO - US02
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoItemResponse> listar() {

        List<Producto> productos =
                productoRepository
                        .buscarPublicablesParaCatalogo(
                                EstadoProducto.ACTIVO
                        );


        if (productos.isEmpty()) {

            return List.of();
        }


        Map<Integer, String> imagenesPrincipales =
                obtenerImagenesPrincipales(
                        productos
                );


        List<Producto> productosIndividuales =
                new ArrayList<>();


        Map<Integer, List<Producto>> productosAgrupados =
                new LinkedHashMap<>();


        for (
                Producto producto
                : productos
        ) {

            GrupoCatalogo grupo =
                    producto.getGrupoCatalogo();


            if (
                    grupo != null
                    &&
                    Boolean.TRUE.equals(
                            grupo.getActivo()
                    )
            ) {

                productosAgrupados
                        .computeIfAbsent(
                                grupo.getIdGrupoCatalogo(),
                                id ->
                                        new ArrayList<>()
                        )
                        .add(
                                producto
                        );


            } else {

                productosIndividuales.add(
                        producto
                );
            }
        }


        List<CatalogoItemResponse> items =
                new ArrayList<>();


        for (
                Producto producto
                : productosIndividuales
        ) {

            items.add(
                    crearItemProducto(
                            producto,
                            imagenesPrincipales
                    )
            );
        }


        for (
                List<Producto> opciones
                : productosAgrupados.values()
        ) {

            items.add(
                    crearItemGrupo(
                            opciones,
                            imagenesPrincipales
                    )
            );
        }


        items.sort(
                Comparator.comparing(
                        CatalogoItemResponse::nombre,
                        String.CASE_INSENSITIVE_ORDER
                )
        );


        return items;
    }


    // =========================================
    // PRODUCTO INDIVIDUAL
    // =========================================

    private CatalogoItemResponse crearItemProducto(
            Producto producto,
            Map<Integer, String> imagenesPrincipales
    ) {

        String imagenUrl =
                imagenesPrincipales.get(
                        producto.getIdProducto()
                );


        boolean disponible =
                producto.getStockActual() != null
                &&
                producto.getStockActual() > 0;


        return new CatalogoItemResponse(

                TipoItemCatalogo.PRODUCTO,

                producto.getIdProducto(),

                null,

                producto.getNombre(),

                imagenUrl,

                producto.getPrecioVenta(),

                false,

                disponible,

                1
        );
    }


    // =========================================
    // GRUPO DE CATÁLOGO
    // =========================================

    private CatalogoItemResponse crearItemGrupo(
            List<Producto> opciones,
            Map<Integer, String> imagenesPrincipales
    ) {

        Producto primerProducto =
                opciones.get(0);


        GrupoCatalogo grupo =
                primerProducto
                        .getGrupoCatalogo();


        BigDecimal precioMinimo =
                opciones
                        .stream()
                        .map(
                                Producto::getPrecioVenta
                        )
                        .min(
                                BigDecimal::compareTo
                        )
                        .orElse(
                                BigDecimal.ZERO
                        );


        boolean disponible =
                opciones
                        .stream()
                        .anyMatch(
                                producto ->
                                        producto.getStockActual()
                                                != null
                                        &&
                                        producto.getStockActual()
                                                > 0
                        );


        String imagenUrl =
                obtenerImagenGrupo(
                        grupo,
                        opciones,
                        imagenesPrincipales
                );


        return new CatalogoItemResponse(

                TipoItemCatalogo.GRUPO,

                null,

                grupo.getIdGrupoCatalogo(),

                grupo.getNombreVisible(),

                imagenUrl,

                precioMinimo,

                opciones.size() > 1,

                disponible,

                opciones.size()
        );
    }


    // =========================================
    // IMAGEN DEL GRUPO
    // =========================================

    private String obtenerImagenGrupo(
            GrupoCatalogo grupo,
            List<Producto> opciones,
            Map<Integer, String> imagenesPrincipales
    ) {

        if (
                grupo.getImagenUrl() != null
                &&
                !grupo.getImagenUrl()
                        .trim()
                        .isEmpty()
        ) {

            return grupo
                    .getImagenUrl()
                    .trim();
        }


        for (
                Producto producto
                : opciones
        ) {

            String imagen =
                    imagenesPrincipales.get(
                            producto.getIdProducto()
                    );


            if (
                    imagen != null
                    &&
                    !imagen.isBlank()
            ) {

                return imagen;
            }
        }


        return null;
    }


    // =========================================
    // IMÁGENES PRINCIPALES
    // =========================================

    private Map<Integer, String>
    obtenerImagenesPrincipales(
            List<Producto> productos
    ) {

        List<Integer> idsProductos =
                productos
                        .stream()
                        .map(
                                Producto::getIdProducto
                        )
                        .toList();


        if (
                idsProductos.isEmpty()
        ) {

            return Map.of();
        }


        List<ImagenProducto> imagenes =
                imagenProductoRepository
                        .buscarPrincipalesPorProductos(
                                idsProductos
                        );


        Map<Integer, String> resultado =
                new LinkedHashMap<>();


        for (
                ImagenProducto imagen
                : imagenes
        ) {

            resultado.put(
                    imagen
                            .getProducto()
                            .getIdProducto(),

                    imagen.getUrlImagen()
            );
        }


        return resultado;
    }
}