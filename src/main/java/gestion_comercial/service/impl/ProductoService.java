package gestion_comercial.service.impl;

import gestion_comercial.dto.request.ProductoCreateRequest;
import gestion_comercial.dto.request.ProductoUpdateRequest;
import gestion_comercial.dto.response.ProductoResponse;


import gestion_comercial.dto.response.ProductoConsultaResponse;

import gestion_comercial.mapper.ProductoConsultaMapper;

import java.util.List;

import gestion_comercial.entity.Categoria;
import gestion_comercial.entity.EstadoProducto;
import gestion_comercial.entity.GrupoCatalogo;
import gestion_comercial.entity.Marca;
import gestion_comercial.entity.Producto;

import gestion_comercial.exception.CategoriaNoEncontradaException;
import gestion_comercial.exception.GrupoCatalogoNoEncontradoException;
import gestion_comercial.exception.MarcaNoEncontradaException;
import gestion_comercial.exception.ProductoNoEncontradoException;

import gestion_comercial.mapper.ProductoMapper;

import gestion_comercial.repository.CategoriaRespository;
import gestion_comercial.repository.GrupoCatalogoRepository;
import gestion_comercial.repository.MarcaRespository;
import gestion_comercial.repository.ProductoRepository;

import gestion_comercial.service.interfaces.IProductoService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ProductoService
        implements IProductoService {

    private final ProductoRepository
            productoRepository;

    private final CategoriaRespository
            categoriaRepository;

    private final MarcaRespository
            marcaRepository;

    private final GrupoCatalogoRepository
            grupoCatalogoRepository;


    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRespository categoriaRepository,
            MarcaRespository marcaRepository,
            GrupoCatalogoRepository grupoCatalogoRepository
    ) {

        this.productoRepository =
                productoRepository;

        this.categoriaRepository =
                categoriaRepository;

        this.marcaRepository =
                marcaRepository;

        this.grupoCatalogoRepository =
                grupoCatalogoRepository;
    }


    // =========================================
    // CREAR PRODUCTO - US27
    // =========================================

    @Override
    @Transactional
    public ProductoResponse crear(
            ProductoCreateRequest request
    ) {

        validarMayorista(
                request.cantidadMinimaMayorista(),
                request.porcentajeDescuentoMayorista()
        );

        validarOferta(
                request.enOferta(),
                request.porcentajeDescuentoOferta()
        );


        Producto producto =
                ProductoMapper.toEntity(
                        request
                );


        producto.setNombre(
                request.nombre().trim()
        );


        producto.setDescripcion(
                limpiarDescripcion(
                        request.descripcion()
                )
        );


        asignarCategoria(
                producto,
                request.idCategoria()
        );


        asignarMarca(
                producto,
                request.idMarca()
        );


        asignarGrupoCatalogo(
                producto,
                request.idGrupoCatalogo()
        );


        Producto productoGuardado =
                productoRepository.saveAndFlush(
                        producto
                );


        return ProductoMapper.toResponse(
                productoGuardado
        );
    }


    // =========================================
    // OBTENER PRODUCTO POR ID - US28
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(
            Integer id
    ) {

        Producto producto =
                buscarProducto(
                        id
                );


        return ProductoMapper.toResponse(
                producto
        );
    }



        // =========================================
        // CONSULTAR PRODUCTOS Y STOCK - US23
        // =========================================

        @Override
        @Transactional(readOnly = true)
        public List<ProductoConsultaResponse> consultar(
                Integer idProducto,
                String nombre,
                Integer idCategoria,
                Integer idMarca,
                EstadoProducto estado,
                Boolean bajoStock
        ) {

        validarIdFiltro(
                idProducto,
                "El ID del producto"
        );


        validarIdFiltro(
                idCategoria,
                "El ID de la categoría"
        );


        validarIdFiltro(
                idMarca,
                "El ID de la marca"
        );


        String nombreBusqueda =
                normalizarNombreBusqueda(
                        nombre
                );


        return productoRepository
                .consultar(

                        idProducto,

                        nombreBusqueda,

                        idCategoria,

                        idMarca,

                        estado,

                        Boolean.TRUE.equals(
                                bajoStock
                        )
                )
                .stream()
                .map(
                        ProductoConsultaMapper::toResponse
                )
                .toList();
        }


    // =========================================
    // MODIFICAR PRODUCTO - US28 / US63
    // =========================================

    @Override
    @Transactional
    public ProductoResponse modificar(
            Integer id,
            ProductoUpdateRequest request
    ) {

        Producto producto =
                buscarProducto(
                        id
                );


        validarMayorista(
                request.cantidadMinimaMayorista(),
                request.porcentajeDescuentoMayorista()
        );


        validarOferta(
                request.enOferta(),
                request.porcentajeDescuentoOferta()
        );


        producto.setNombre(
                request.nombre().trim()
        );


        producto.setDescripcion(
                limpiarDescripcion(
                        request.descripcion()
                )
        );


        producto.setCategoria(
                obtenerCategoria(
                        request.idCategoria()
                )
        );


        producto.setMarca(
                obtenerMarca(
                        request.idMarca()
                )
        );


        producto.setGrupoCatalogo(
                resolverGrupoCatalogoParaModificacion(
                        producto,
                        request.idGrupoCatalogo()
                )
        );


        producto.setPrecioCosto(
                request.precioCosto()
        );


        producto.setPrecioVenta(
                request.precioVenta()
        );


        if (
                request.stockMinimo()
                        != null
        ) {

            producto.setStockMinimo(
                    request.stockMinimo()
            );
        }


        producto.setCantidadMinimaMayorista(
                request.cantidadMinimaMayorista()
        );


        producto.setPorcentajeDescuentoMayorista(
                request.porcentajeDescuentoMayorista()
        );


        producto.setEnOferta(
                request.enOferta()
        );


        if (
                Boolean.TRUE.equals(
                        request.enOferta()
                )
        ) {

            producto.setPorcentajeDescuentoOferta(
                    request.porcentajeDescuentoOferta()
            );

        } else {

            producto.setPorcentajeDescuentoOferta(
                    null
            );
        }


        producto.setEstado(
                request.estado()
        );


        producto.setPublicadoOnline(
                request.publicadoOnline()
        );


        Producto productoGuardado =
                productoRepository.saveAndFlush(
                        producto
                );


        return ProductoMapper.toResponse(
                productoGuardado
        );
    }


    // =========================================
    // PAUSAR PRODUCTO - US29
    // =========================================

    @Override
    @Transactional
    public ProductoResponse pausar(
            Integer id
    ) {

        Producto producto =
                buscarProducto(
                        id
                );


        if (
                producto.getEstado()
                        ==
                EstadoProducto.PAUSADO
        ) {

            throw new IllegalArgumentException(
                    "El producto ya se encuentra pausado"
            );
        }


        if (
                producto.getEstado()
                        ==
                EstadoProducto.DISCONTINUADO
        ) {

            throw new IllegalArgumentException(
                    "Un producto discontinuado no puede pausarse"
            );
        }


        producto.setEstado(
                EstadoProducto.PAUSADO
        );


        Producto productoGuardado =
                productoRepository.saveAndFlush(
                        producto
                );


        return ProductoMapper.toResponse(
                productoGuardado
        );
    }


    // =========================================
    // DISCONTINUAR PRODUCTO - US29
    // =========================================

    @Override
    @Transactional
    public ProductoResponse discontinuar(
            Integer id
    ) {

        Producto producto =
                buscarProducto(
                        id
                );


        if (
                producto.getEstado()
                        ==
                EstadoProducto.DISCONTINUADO
        ) {

            throw new IllegalArgumentException(
                    "El producto ya se encuentra discontinuado"
            );
        }


        producto.setEstado(
                EstadoProducto.DISCONTINUADO
        );


        Producto productoGuardado =
                productoRepository.saveAndFlush(
                        producto
                );


        return ProductoMapper.toResponse(
                productoGuardado
        );
    }


    // =========================================
    // PRODUCTO
    // =========================================

    private Producto buscarProducto(
            Integer id
    ) {

        return productoRepository
                .findById(
                        id
                )
                .orElseThrow(
                        () ->
                                new ProductoNoEncontradoException(
                                        "No existe un producto con este ID"
                                )
                );
    }


    // =========================================
    // MAYORISTA
    // =========================================

    private void validarMayorista(
            Integer cantidadMinima,
            java.math.BigDecimal porcentaje
    ) {

        boolean tieneCantidad =
                cantidadMinima != null;

        boolean tienePorcentaje =
                porcentaje != null;


        if (
                tieneCantidad
                        !=
                tienePorcentaje
        ) {

            throw new IllegalArgumentException(
                    "Para configurar descuento mayorista debe indicar cantidad mínima y porcentaje de descuento"
            );
        }
    }


    // =========================================
    // OFERTA
    // =========================================

    private void validarOferta(
            Boolean enOferta,
            java.math.BigDecimal porcentaje
    ) {

        boolean ofertaActiva =
                Boolean.TRUE.equals(
                        enOferta
                );


        boolean tienePorcentaje =
                porcentaje != null;


        if (
                ofertaActiva
                        &&
                !tienePorcentaje
        ) {

            throw new IllegalArgumentException(
                    "Debe indicar el porcentaje de descuento de la oferta"
            );
        }


        if (
                !ofertaActiva
                        &&
                tienePorcentaje
        ) {

            throw new IllegalArgumentException(
                    "No puede indicar un porcentaje de oferta si el producto no está en oferta"
            );
        }
    }


    // =========================================
    // CATEGORÍA
    // =========================================

    private void asignarCategoria(
            Producto producto,
            Integer idCategoria
    ) {

        if (idCategoria == null) {
            return;
        }


        producto.setCategoria(
                obtenerCategoria(
                        idCategoria
                )
        );
    }


    private Categoria obtenerCategoria(
            Integer idCategoria
    ) {

        if (idCategoria == null) {
            return null;
        }


        Categoria categoria =
                categoriaRepository
                        .findById(
                                idCategoria
                        )
                        .orElseThrow(
                                () ->
                                        new CategoriaNoEncontradaException(
                                                "No se encontró la categoría seleccionada"
                                        )
                        );


        if (
                !Boolean.TRUE.equals(
                        categoria.getActiva()
                )
        ) {

            throw new IllegalArgumentException(
                    "La categoría seleccionada está inactiva"
            );
        }


        return categoria;
    }


    // =========================================
    // MARCA
    // =========================================

    private void asignarMarca(
            Producto producto,
            Integer idMarca
    ) {

        if (idMarca == null) {
            return;
        }


        producto.setMarca(
                obtenerMarca(
                        idMarca
                )
        );
    }


    private Marca obtenerMarca(
            Integer idMarca
    ) {

        if (idMarca == null) {
            return null;
        }


        Marca marca =
                marcaRepository
                        .findById(
                                idMarca
                        )
                        .orElseThrow(
                                () ->
                                        new MarcaNoEncontradaException(
                                                "No se encontró la marca seleccionada"
                                        )
                        );


        if (
                !Boolean.TRUE.equals(
                        marca.getActiva()
                )
        ) {

            throw new IllegalArgumentException(
                    "La marca seleccionada está inactiva"
            );
        }


        return marca;
    }


    // =========================================
    // GRUPO DE CATÁLOGO - US63
    // =========================================

    private void asignarGrupoCatalogo(
            Producto producto,
            Integer idGrupoCatalogo
    ) {

        if (idGrupoCatalogo == null) {
            return;
        }


        producto.setGrupoCatalogo(
                obtenerGrupoCatalogoActivo(
                        idGrupoCatalogo
                )
        );
    }


    private GrupoCatalogo resolverGrupoCatalogoParaModificacion(
            Producto producto,
            Integer idGrupoCatalogo
    ) {

        if (
                idGrupoCatalogo == null
        ) {

            return null;
        }


        GrupoCatalogo grupoActual =
                producto.getGrupoCatalogo();


        if (
                grupoActual != null
                        &&
                grupoActual
                        .getIdGrupoCatalogo()
                        .equals(
                                idGrupoCatalogo
                        )
        ) {

            return grupoActual;
        }


        return obtenerGrupoCatalogoActivo(
                idGrupoCatalogo
        );
    }


    private GrupoCatalogo obtenerGrupoCatalogoActivo(
            Integer idGrupoCatalogo
    ) {

        GrupoCatalogo grupoCatalogo =
                grupoCatalogoRepository
                        .findById(
                                idGrupoCatalogo
                        )
                        .orElseThrow(
                                () ->
                                        new GrupoCatalogoNoEncontradoException(
                                                "No se encontró el grupo de catálogo seleccionado"
                                        )
                        );


        if (
                !Boolean.TRUE.equals(
                        grupoCatalogo.getActivo()
                )
        ) {

            throw new IllegalArgumentException(
                    "El grupo de catálogo seleccionado está inactivo"
            );
        }


        return grupoCatalogo;
    }


    // =========================================
    // DESCRIPCIÓN
    // =========================================

    private String limpiarDescripcion(
            String descripcion
    ) {

        if (
                descripcion == null
                        ||
                descripcion.trim().isEmpty()
        ) {

            return null;
        }


        return descripcion.trim();
    }






        // =========================================
        // VALIDACIONES CONSULTA - US23
        // =========================================

        private void validarIdFiltro(
                Integer id,
                String nombreCampo
        ) {

        if (
                id != null
                        &&
                id <= 0
        ) {

                throw new IllegalArgumentException(
                        nombreCampo
                                + " debe ser un número entero mayor a cero"
                );
        }
        }


        private String normalizarNombreBusqueda(
                String nombre
        ) {

        if (nombre == null) {

                return null;
        }


        if (nombre.isEmpty()) {

                return null;
        }


        if (
                nombre.trim().isEmpty()
        ) {

                throw new IllegalArgumentException(
                        "El nombre de búsqueda no puede contener solo espacios"
                );
        }


        String nombreLimpio =
                nombre.trim();


        if (
                !nombreLimpio.matches(
                        ".*\\p{L}.*"
                )
        ) {

                throw new IllegalArgumentException(
                        "El nombre de búsqueda debe contener al menos una letra"
                );
        }


        return nombreLimpio;
        }
}