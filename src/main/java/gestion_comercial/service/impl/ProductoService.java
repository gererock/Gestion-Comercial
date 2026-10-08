package gestion_comercial.service.impl;

import gestion_comercial.dto.request.ProductoCreateRequest;
import gestion_comercial.dto.response.ProductoResponse;

import gestion_comercial.entity.Categoria;
import gestion_comercial.entity.GrupoCatalogo;
import gestion_comercial.entity.Marca;
import gestion_comercial.entity.Producto;

import gestion_comercial.exception.CategoriaNoEncontradaException;
import gestion_comercial.exception.GrupoCatalogoNoEncontradoException;
import gestion_comercial.exception.MarcaNoEncontradaException;

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


    @Override
    @Transactional
    public ProductoResponse crear(
            ProductoCreateRequest request
    ) {

        validarMayorista(request);

        validarOferta(request);


        Producto producto =
                ProductoMapper.toEntity(
                        request
                );


        producto.setNombre(
                request.nombre().trim()
        );


        if (
                request.descripcion() == null
                        || request.descripcion()
                                .trim()
                                .isEmpty()
        ) {

            producto.setDescripcion(
                    null
            );

        } else {

            producto.setDescripcion(
                    request.descripcion().trim()
            );
        }


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


    private void validarMayorista(
            ProductoCreateRequest request
    ) {

        boolean tieneCantidad =
                request.cantidadMinimaMayorista()
                        != null;

        boolean tienePorcentaje =
                request.porcentajeDescuentoMayorista()
                        != null;


        if (tieneCantidad != tienePorcentaje) {

            throw new IllegalArgumentException(
                    "Para configurar descuento mayorista debe indicar cantidad mínima y porcentaje de descuento"
            );
        }
    }


    private void validarOferta(
            ProductoCreateRequest request
    ) {

        boolean enOferta =
                Boolean.TRUE.equals(
                        request.enOferta()
                );

        boolean tienePorcentaje =
                request.porcentajeDescuentoOferta()
                        != null;


        if (
                enOferta
                        && !tienePorcentaje
        ) {

            throw new IllegalArgumentException(
                    "Debe indicar el porcentaje de descuento de la oferta"
            );
        }


        if (
                !enOferta
                        && tienePorcentaje
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


        Categoria categoria =
                categoriaRepository
                        .findById(idCategoria)
                        .orElseThrow(
                                () ->
                                        new CategoriaNoEncontradaException(
                                                "No se encontró la categoría seleccionada"
                                        )
                        );


        if (!Boolean.TRUE.equals(
                categoria.getActiva()
        )) {

            throw new IllegalArgumentException(
                    "La categoría seleccionada está inactiva"
            );
        }


        producto.setCategoria(
                categoria
        );
    }


    private void asignarMarca(
            Producto producto,
            Integer idMarca
    ) {

        if (idMarca == null) {
            return;
        }


        Marca marca =
                marcaRepository
                        .findById(idMarca)
                        .orElseThrow(
                                () ->
                                        new MarcaNoEncontradaException(
                                                "No se encontró la marca seleccionada"
                                        )
                        );


        if (!Boolean.TRUE.equals(
                marca.getActiva()
        )) {

            throw new IllegalArgumentException(
                    "La marca seleccionada está inactiva"
            );
        }


        producto.setMarca(
                marca
        );
    }


    private void asignarGrupoCatalogo(
            Producto producto,
            Integer idGrupoCatalogo
    ) {

        if (idGrupoCatalogo == null) {
            return;
        }


        GrupoCatalogo grupoCatalogo =
                grupoCatalogoRepository
                        .findById(idGrupoCatalogo)
                        .orElseThrow(
                                () ->
                                        new GrupoCatalogoNoEncontradoException(
                                                "No se encontró el grupo de catálogo seleccionado"
                                        )
                        );


        if (!Boolean.TRUE.equals(
                grupoCatalogo.getActivo()
        )) {

            throw new IllegalArgumentException(
                    "El grupo de catálogo seleccionado está inactivo"
            );
        }


        producto.setGrupoCatalogo(
                grupoCatalogo
        );
    }
}