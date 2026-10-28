package gestion_comercial.service.impl;

import gestion_comercial.dto.request.CategoriaGastoCreateRequest;
import gestion_comercial.dto.request.CategoriaGastoUpdateRequest;
import gestion_comercial.dto.response.CategoriaGastoResponse;
import gestion_comercial.entity.CategoriaGasto;
import gestion_comercial.exception.CategoriaGastoDuplicadaException;
import gestion_comercial.exception.CategoriaGastoNoEncontradaException;
import gestion_comercial.mapper.CategoriaGastoMapper;
import gestion_comercial.repository.CategoriaGastoRepository;
import gestion_comercial.service.interfaces.ICategoriaGastoService;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaGastoService
        implements ICategoriaGastoService {

    private final CategoriaGastoRepository
            categoriaGastoRepository;


    @Override
    @Transactional
    public CategoriaGastoResponse crear(
            CategoriaGastoCreateRequest request
    ) {

        String nombre =
                request.nombre().trim();


        if (
                categoriaGastoRepository
                        .existsByNombreIgnoreCase(
                                nombre
                        )
        ) {

            throw new CategoriaGastoDuplicadaException(
                    "Ya existe una categoría de gasto con ese nombre"
            );
        }


        CategoriaGasto categoria =
                CategoriaGastoMapper.toEntity(
                        request
                );


        categoria.setNombre(
                nombre
        );


        categoria.setDescripcion(
                normalizarDescripcion(
                        request.descripcion()
                )
        );


        categoria.setActiva(
                true
        );


        try {

            CategoriaGasto guardada =
                    categoriaGastoRepository.saveAndFlush(categoria);


            return CategoriaGastoMapper.toResponse(
                    guardada
            );

        } catch (
                DataIntegrityViolationException exception
        ) {

            throw new CategoriaGastoDuplicadaException(
                    "Ya existe una categoría de gasto con ese nombre"
            );
        }
    }


    @Override
    @Transactional(readOnly = true)
    public CategoriaGastoResponse buscarPorId(
            Integer idCategoriaGasto
    ) {

        CategoriaGasto categoria =
                obtenerCategoria(
                        idCategoriaGasto
                );


        return CategoriaGastoMapper.toResponse(
                categoria
        );
    }


    @Override
    @Transactional
    public CategoriaGastoResponse actualizar(
            Integer idCategoriaGasto,
            CategoriaGastoUpdateRequest request
    ) {

        CategoriaGasto categoria =
                obtenerCategoria(
                        idCategoriaGasto
                );


        String nombre =
                request.nombre().trim();


        if (
                categoriaGastoRepository
                        .existsByNombreIgnoreCaseAndIdCategoriaGastoNot(
                                nombre,
                                idCategoriaGasto
                        )
        ) {

            throw new CategoriaGastoDuplicadaException(
                    "Ya existe otra categoría de gasto con ese nombre"
            );
        }


        categoria.setNombre(
                nombre
        );


        categoria.setDescripcion(
                normalizarDescripcion(
                        request.descripcion()
                )
        );


        try {

            CategoriaGasto actualizada =
                    categoriaGastoRepository.saveAndFlush(categoria);


            return CategoriaGastoMapper.toResponse(
                    actualizada
            );

        } catch (
                DataIntegrityViolationException exception
        ) {

            throw new CategoriaGastoDuplicadaException(
                    "Ya existe otra categoría de gasto con ese nombre"
            );
        }
    }


    @Override
    @Transactional
    public CategoriaGastoResponse cambiarEstado(
            Integer idCategoriaGasto,
            Boolean activa
    ) {

        CategoriaGasto categoria =
                obtenerCategoria(
                        idCategoriaGasto
                );


        categoria.setActiva(
                activa
        );


        CategoriaGasto actualizada =
                categoriaGastoRepository.save(
                        categoria
                );


        return CategoriaGastoMapper.toResponse(
                actualizada
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<CategoriaGastoResponse> buscar(
            String nombre,
            Boolean activa
    ) {

        validarBusqueda(
                nombre
        );


        List<CategoriaGasto> categorias;


        if (
                nombre == null
                &&
                activa == null
        ) {

            categorias =
                    categoriaGastoRepository.findAll();

        } else if (
                nombre != null
                &&
                activa == null
        ) {

            categorias =
                    categoriaGastoRepository
                            .findByNombreContainingIgnoreCase(
                                    nombre.trim()
                            );

        } else if (
                nombre == null
        ) {

            categorias =
                    categoriaGastoRepository
                            .findByActiva(
                                    activa
                            );

        } else {

            categorias =
                    categoriaGastoRepository
                            .findByNombreContainingIgnoreCaseAndActiva(
                                    nombre.trim(),
                                    activa
                            );
        }


        return categorias
                .stream()
                .map(
                        CategoriaGastoMapper::toResponse
                )
                .toList();
    }


    private CategoriaGasto obtenerCategoria(
            Integer idCategoriaGasto
    ) {

        return categoriaGastoRepository
                .findById(
                        idCategoriaGasto
                )
                .orElseThrow(
                        () ->
                                new CategoriaGastoNoEncontradaException(
                                        "La categoría de gasto indicada no existe"
                                )
                );
    }


    private void validarBusqueda(
            String nombre
    ) {

        if (
                nombre != null
                &&
                !nombre.isEmpty()
                &&
                nombre.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "La búsqueda no puede contener solamente espacios"
            );
        }
    }


    private String normalizarDescripcion(
            String descripcion
    ) {

        if (
                descripcion == null
        ) {

            return null;
        }


        String valor =
                descripcion.trim();


        return valor.isEmpty()
                ? null
                : valor;
    }
}