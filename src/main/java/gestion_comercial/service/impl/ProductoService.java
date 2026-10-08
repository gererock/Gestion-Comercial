package gestion_comercial.service.impl;

import gestion_comercial.dto.request.ProductoCreateRequest;
import gestion_comercial.dto.response.ProductoResponse;
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

        Producto producto =
                ProductoMapper.toEntity(
                        request
                );


        if (request.idCategoria() != null) {

            producto.setCategoria(
                    categoriaRepository
                            .findById(
                                    request.idCategoria()
                            )
                            .orElseThrow(
                                    () ->
                                            new CategoriaNoEncontradaException(
                                                    "No se encontró la categoría seleccionada"
                                            )
                            )
            );
        }


        if (request.idMarca() != null) {

            producto.setMarca(
                    marcaRepository
                            .findById(
                                    request.idMarca()
                            )
                            .orElseThrow(
                                    () ->
                                            new MarcaNoEncontradaException(
                                                    "No se encontró la marca seleccionada"
                                            )
                            )
            );
        }


        if (request.idGrupoCatalogo() != null) {

            producto.setGrupoCatalogo(
                    grupoCatalogoRepository
                            .findById(
                                    request.idGrupoCatalogo()
                            )
                            .orElseThrow(
                                    () ->
                                            new GrupoCatalogoNoEncontradoException(
                                                    "No se encontró el grupo de catálogo seleccionado"
                                            )
                            )
            );
        }


        Producto productoGuardado =
                productoRepository.saveAndFlush(
                        producto
                );


        return ProductoMapper.toResponse(
                productoGuardado
        );
    }
}