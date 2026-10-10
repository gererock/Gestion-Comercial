package gestion_comercial.service.impl;

import gestion_comercial.dto.request.ProductoCreateRequest;
import gestion_comercial.dto.request.ProductoUpdateRequest;
import gestion_comercial.dto.response.ProductoResponse;

import gestion_comercial.entity.Categoria;
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
                buscarProducto(id);


        return ProductoMapper.toResponse(
                producto
        );
    }



    @Override
    @Transactional
    public ProductoResponse modificar(
            Integer id,
            ProductoUpdateRequest request
    ) {

        Producto producto =
                buscarProducto(id);


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
                obtenerGrupoCatalogo(
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


    private Producto buscarProducto(
            Integer id
    ) {

        return productoRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new ProductoNoEncontradoException(
                                        "No existe un producto con este ID"
                                )
                );
    }




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



    private void asignarGrupoCatalogo(
            Producto producto,
            Integer idGrupoCatalogo
    ) {

        if (idGrupoCatalogo == null) {
            return;
        }


        producto.setGrupoCatalogo(
                obtenerGrupoCatalogo(
                        idGrupoCatalogo
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

    private GrupoCatalogo obtenerGrupoCatalogo(
            Integer idGrupoCatalogo
    ) {

        if (
                idGrupoCatalogo == null
        ) {

            return null;
        }


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
}