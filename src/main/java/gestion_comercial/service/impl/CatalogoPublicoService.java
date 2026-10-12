package gestion_comercial.service.impl;

import gestion_comercial.dto.response.CatalogoFiltroOpcionResponse;
import gestion_comercial.dto.response.CatalogoFiltrosResponse;
import gestion_comercial.dto.response.CatalogoImagenResponse;
import gestion_comercial.dto.response.CatalogoItemResponse;
import gestion_comercial.dto.response.CatalogoProductoDetalleResponse;

import gestion_comercial.entity.Categoria;
import gestion_comercial.entity.EstadoProducto;
import gestion_comercial.entity.GrupoCatalogo;
import gestion_comercial.entity.ImagenProducto;
import gestion_comercial.entity.Marca;
import gestion_comercial.entity.Producto;
import gestion_comercial.entity.TipoItemCatalogo;

import gestion_comercial.exception.ProductoNoEncontradoException;

import gestion_comercial.repository.CategoriaRespository;
import gestion_comercial.repository.ImagenProductoRepository;
import gestion_comercial.repository.MarcaRespository;
import gestion_comercial.repository.ProductoRepository;

import gestion_comercial.service.interfaces.ICatalogoPublicoService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatalogoPublicoService
                implements ICatalogoPublicoService {

        private final ProductoRepository productoRepository;

        private final ImagenProductoRepository imagenProductoRepository;

        private final CategoriaRespository categoriaRespository;

        private final MarcaRespository marcaRespository;

        public CatalogoPublicoService(
                        ProductoRepository productoRepository,
                        ImagenProductoRepository imagenProductoRepository,
                        CategoriaRespository categoriaRespository,
                        MarcaRespository marcaRespository) {

                this.productoRepository = productoRepository;

                this.imagenProductoRepository = imagenProductoRepository;

                this.categoriaRespository = categoriaRespository;

                this.marcaRespository = marcaRespository;
        }

        // =========================================
        // CATÁLOGO GENERAL
        // =========================================

        @Override
        @Transactional(readOnly = true)
        public List<CatalogoItemResponse> listar() {

                return listar(
                                null,
                                null,
                                null);
        }

        // =========================================
        // CATÁLOGO PÚBLICO CON FILTROS - US03
        // =========================================

        @Override
        @Transactional(readOnly = true)
        public List<CatalogoItemResponse> listar(
                        String nombre,
                        Integer idCategoria,
                        Integer idMarca) {

                String nombreNormalizado = normalizarNombreBusqueda(
                                nombre);

                String nombreParaConsulta = nombreNormalizado == null
                                ? ""
                                : nombreNormalizado;

                List<Producto> productos = productoRepository
                                .buscarPublicablesParaCatalogoFiltrado(

                                                EstadoProducto.ACTIVO,

                                                nombreParaConsulta,

                                                idCategoria,

                                                idMarca);

                if (productos.isEmpty()) {

                        return List.of();
                }

                Map<Integer, String> imagenesPrincipales = obtenerImagenesPrincipales(
                                productos);

                List<Producto> productosIndividuales = new ArrayList<>();

                Map<Integer, List<Producto>> productosAgrupados = new LinkedHashMap<>();

                for (Producto producto : productos) {

                        GrupoCatalogo grupo = producto.getGrupoCatalogo();

                        if (grupo != null
                                        &&
                                        Boolean.TRUE.equals(
                                                        grupo.getActivo())) {

                                productosAgrupados
                                                .computeIfAbsent(
                                                                grupo.getIdGrupoCatalogo(),
                                                                id -> new ArrayList<>())
                                                .add(
                                                                producto);

                        } else {

                                productosIndividuales.add(
                                                producto);
                        }
                }

                List<CatalogoItemResponse> items = new ArrayList<>();

                for (Producto producto : productosIndividuales) {

                        items.add(
                                        crearItemProducto(
                                                        producto,
                                                        imagenesPrincipales));
                }

                for (List<Producto> opciones : productosAgrupados.values()) {

                        items.add(
                                        crearItemGrupo(
                                                        opciones,
                                                        imagenesPrincipales));
                }

                items.sort(
                                Comparator.comparing(
                                                CatalogoItemResponse::nombre,
                                                String.CASE_INSENSITIVE_ORDER));

                return items;
        }

        // =========================================
        // FILTROS DISPONIBLES - US03
        // =========================================

        @Override
        @Transactional(readOnly = true)
        public CatalogoFiltrosResponse obtenerFiltros() {

                List<CatalogoFiltroOpcionResponse> categorias = categoriaRespository
                                .findByActiva(
                                                true)
                                .stream()
                                .sorted(
                                                Comparator.comparing(
                                                                Categoria::getNombre,
                                                                String.CASE_INSENSITIVE_ORDER))
                                .map(
                                                categoria -> new CatalogoFiltroOpcionResponse(

                                                                categoria.getId(),

                                                                categoria.getNombre()))
                                .toList();

                List<CatalogoFiltroOpcionResponse> marcas = marcaRespository
                                .findByActiva(
                                                true)
                                .stream()
                                .sorted(
                                                Comparator.comparing(
                                                                Marca::getNombre,
                                                                String.CASE_INSENSITIVE_ORDER))
                                .map(
                                                marca -> new CatalogoFiltroOpcionResponse(

                                                                marca.getId(),

                                                                marca.getNombre()))
                                .toList();

                return new CatalogoFiltrosResponse(

                                categorias,

                                marcas);
        }

        // =========================================
        // DETALLE PÚBLICO DEL PRODUCTO - US04
        // =========================================

        @Override
        @Transactional(readOnly = true)
        public CatalogoProductoDetalleResponse obtenerDetalle(
                        Integer idProducto) {

                if (idProducto == null
                                ||
                                idProducto <= 0) {

                        throw new ProductoNoEncontradoException(
                                        "El producto no existe o no está disponible");
                }

                Producto producto = productoRepository
                                .buscarDetallePublico(

                                                idProducto,

                                                EstadoProducto.ACTIVO)
                                .orElseThrow(
                                                () -> new ProductoNoEncontradoException(
                                                                "El producto no existe o no está disponible"));

                List<CatalogoImagenResponse> imagenes = imagenProductoRepository
                                .findByProducto_IdProductoOrderByOrdenAscIdImagenAsc(
                                                idProducto)
                                .stream()
                                .sorted(
                                                Comparator
                                                                .comparing(
                                                                                ImagenProducto::getPrincipal,
                                                                                Comparator.nullsLast(
                                                                                                Comparator.reverseOrder()))
                                                                .thenComparing(
                                                                                ImagenProducto::getOrden,
                                                                                Comparator.nullsLast(
                                                                                                Comparator.naturalOrder()))
                                                                .thenComparing(
                                                                                ImagenProducto::getIdImagen))
                                .map(
                                                imagen -> new CatalogoImagenResponse(

                                                                imagen.getUrlImagen(),

                                                                imagen.getPrincipal(),

                                                                imagen.getOrden()))
                                .toList();

                boolean disponible = producto.getStockActual() != null
                                &&
                                producto.getStockActual() > 0;

                boolean enOferta = Boolean.TRUE.equals(
                                producto.getEnOferta())
                                &&
                                producto.getPorcentajeDescuentoOferta() != null;

                BigDecimal precioOferta = calcularPrecioOferta(
                                producto);

                boolean tieneDescuentoMayorista = producto.getCantidadMinimaMayorista() != null
                                &&
                                producto.getPorcentajeDescuentoMayorista() != null;

                return new CatalogoProductoDetalleResponse(

                                producto.getIdProducto(),

                                producto.getNombre(),

                                producto.getDescripcion(),

                                producto.getPrecioVenta(),

                                disponible,

                                enOferta,

                                enOferta
                                                ? producto.getPorcentajeDescuentoOferta()
                                                : null,

                                precioOferta,

                                tieneDescuentoMayorista,

                                tieneDescuentoMayorista
                                                ? producto.getCantidadMinimaMayorista()
                                                : null,

                                tieneDescuentoMayorista
                                                ? producto.getPorcentajeDescuentoMayorista()
                                                : null,

                                producto.getCategoria() != null
                                                ? producto.getCategoria().getId()
                                                : null,

                                producto.getCategoria() != null
                                                ? producto.getCategoria().getNombre()
                                                : null,

                                producto.getMarca() != null
                                                ? producto.getMarca().getId()
                                                : null,

                                producto.getMarca() != null
                                                ? producto.getMarca().getNombre()
                                                : null,

                                producto.getGrupoCatalogo() != null
                                                ? producto
                                                                .getGrupoCatalogo()
                                                                .getIdGrupoCatalogo()
                                                : null,

                                producto.getGrupoCatalogo() != null
                                                ? producto
                                                                .getGrupoCatalogo()
                                                                .getNombreVisible()
                                                : null,

                                imagenes);
        }

        // =========================================
        // PRECIO DE OFERTA - US04
        // =========================================

        private BigDecimal calcularPrecioOferta(
                        Producto producto) {

                if (!Boolean.TRUE.equals(
                                producto.getEnOferta())
                                ||
                                producto.getPorcentajeDescuentoOferta() == null) {

                        return null;
                }

                BigDecimal porcentajeRestante = BigDecimal
                                .valueOf(
                                                100)
                                .subtract(
                                                producto
                                                                .getPorcentajeDescuentoOferta());

                return producto
                                .getPrecioVenta()
                                .multiply(
                                                porcentajeRestante)
                                .divide(
                                                BigDecimal.valueOf(
                                                                100),
                                                2,
                                                RoundingMode.HALF_UP);
        }

        // =========================================
        // VALIDAR BÚSQUEDA - US03
        // =========================================

        private String normalizarNombreBusqueda(
                        String nombre) {

                if (nombre == null) {

                        return null;
                }

                String nombreNormalizado = nombre.trim();

                if (nombreNormalizado.isEmpty()) {

                        return null;
                }

                if (nombreNormalizado.length() > 120) {

                        throw new IllegalArgumentException(
                                        "La búsqueda no puede superar los 120 caracteres");
                }

                if (!nombreNormalizado.matches(
                                ".*[\\p{L}\\p{N}].*")) {

                        throw new IllegalArgumentException(
                                        "La búsqueda debe contener al menos una letra o un número");
                }

                return nombreNormalizado;
        }

        // =========================================
        // PRODUCTO INDIVIDUAL
        // =========================================

        private CatalogoItemResponse crearItemProducto(
                        Producto producto,
                        Map<Integer, String> imagenesPrincipales) {

                String imagenUrl = imagenesPrincipales.get(
                                producto.getIdProducto());

                boolean disponible = producto.getStockActual() != null
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

                                1);
        }

        // =========================================
        // GRUPO DE CATÁLOGO
        // =========================================

        private CatalogoItemResponse crearItemGrupo(
                        List<Producto> opciones,
                        Map<Integer, String> imagenesPrincipales) {

                Producto primerProducto = opciones.get(0);

                GrupoCatalogo grupo = primerProducto
                                .getGrupoCatalogo();

                BigDecimal precioMinimo = opciones
                                .stream()
                                .map(
                                                Producto::getPrecioVenta)
                                .min(
                                                BigDecimal::compareTo)
                                .orElse(
                                                BigDecimal.ZERO);

                boolean disponible = opciones
                                .stream()
                                .anyMatch(
                                                producto -> producto.getStockActual() != null
                                                                &&
                                                                producto.getStockActual() > 0);

                String imagenUrl = obtenerImagenGrupo(
                                grupo,
                                opciones,
                                imagenesPrincipales);

                return new CatalogoItemResponse(

                                TipoItemCatalogo.GRUPO,

                                null,

                                grupo.getIdGrupoCatalogo(),

                                grupo.getNombreVisible(),

                                imagenUrl,

                                precioMinimo,

                                opciones.size() > 1,

                                disponible,

                                opciones.size());
        }

        // =========================================
        // IMAGEN DEL GRUPO
        // =========================================

        private String obtenerImagenGrupo(
                        GrupoCatalogo grupo,
                        List<Producto> opciones,
                        Map<Integer, String> imagenesPrincipales) {

                if (grupo.getImagenUrl() != null
                                &&
                                !grupo.getImagenUrl()
                                                .trim()
                                                .isEmpty()) {

                        return grupo
                                        .getImagenUrl()
                                        .trim();
                }

                for (Producto producto : opciones) {

                        String imagen = imagenesPrincipales.get(
                                        producto.getIdProducto());

                        if (imagen != null
                                        &&
                                        !imagen.isBlank()) {

                                return imagen;
                        }
                }

                return null;
        }

        // =========================================
        // IMÁGENES PRINCIPALES
        // =========================================

        private Map<Integer, String> obtenerImagenesPrincipales(
                        List<Producto> productos) {

                List<Integer> idsProductos = productos
                                .stream()
                                .map(
                                                Producto::getIdProducto)
                                .toList();

                if (idsProductos.isEmpty()) {

                        return Map.of();
                }

                List<ImagenProducto> imagenes = imagenProductoRepository
                                .buscarPrincipalesPorProductos(
                                                idsProductos);

                Map<Integer, String> resultado = new LinkedHashMap<>();

                for (ImagenProducto imagen : imagenes) {

                        resultado.put(
                                        imagen
                                                        .getProducto()
                                                        .getIdProducto(),

                                        imagen.getUrlImagen());
                }

                return resultado;
        }
}