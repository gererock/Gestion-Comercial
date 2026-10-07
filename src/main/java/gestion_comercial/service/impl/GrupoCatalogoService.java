package gestion_comercial.service.impl;

import gestion_comercial.dto.request.GrupoCatalogoCreateRequest;
import gestion_comercial.dto.request.GrupoCatalogoUpdateRequest;
import gestion_comercial.dto.response.GrupoCatalogoResponse;
import gestion_comercial.entity.GrupoCatalogo;
import gestion_comercial.exception.GrupoCatalogoDuplicadoException;
import gestion_comercial.exception.GrupoCatalogoNoEncontradoException;
import gestion_comercial.mapper.GrupoCatalogoMapper;
import gestion_comercial.repository.GrupoCatalogoRepository;
import gestion_comercial.service.interfaces.IGrupoCatalogoService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrupoCatalogoService
        implements IGrupoCatalogoService {

    private final GrupoCatalogoRepository
            grupoCatalogoRepository;


    public GrupoCatalogoService(
            GrupoCatalogoRepository grupoCatalogoRepository
    ) {
        this.grupoCatalogoRepository =
                grupoCatalogoRepository;
    }


    @Override
    public GrupoCatalogoResponse crear(
            GrupoCatalogoCreateRequest request
    ) {

        String nombreVisible =
                request.nombreVisible().trim();

        if (
                grupoCatalogoRepository
                        .existsByNombreVisibleIgnoreCase(
                                nombreVisible
                        )
        ) {
            throw new GrupoCatalogoDuplicadoException(
                    "Ya existe un grupo de catálogo con ese nombre"
            );
        }


        GrupoCatalogo grupo =
                GrupoCatalogoMapper.toEntity(request);

        grupo.setNombreVisible(
                nombreVisible
        );


        try {

            GrupoCatalogo grupoGuardado =
                    grupoCatalogoRepository.save(grupo);

            return GrupoCatalogoMapper.toResponse(
                    grupoGuardado
            );

        } catch (DataIntegrityViolationException exception) {

            throw new GrupoCatalogoDuplicadoException(
                    "Ya existe un grupo de catálogo con ese nombre"
            );
        }
    }


    @Override
    public List<GrupoCatalogoResponse> listar() {

        return grupoCatalogoRepository
                .findAll()
                .stream()
                .map(
                        GrupoCatalogoMapper::toResponse
                )
                .toList();
    }


    @Override
    public GrupoCatalogoResponse buscarPorId(
            Integer id
    ) {

        GrupoCatalogo grupo =
                buscarEntidadPorId(id);

        return GrupoCatalogoMapper.toResponse(
                grupo
        );
    }


    @Override
    public GrupoCatalogoResponse actualizar(
            Integer id,
            GrupoCatalogoUpdateRequest request
    ) {

        GrupoCatalogo grupo =
                buscarEntidadPorId(id);


        String nombreVisible =
                request.nombreVisible().trim();


        if (
                grupoCatalogoRepository
                        .existsByNombreVisibleIgnoreCaseAndIdGrupoCatalogoNot(
                                nombreVisible,
                                id
                        )
        ) {
            throw new GrupoCatalogoDuplicadoException(
                    "Ya existe un grupo de catálogo con ese nombre"
            );
        }


        GrupoCatalogoMapper.updateEntity(
                grupo,
                request
        );

        grupo.setNombreVisible(
                nombreVisible
        );


        try {

            GrupoCatalogo grupoActualizado =
                    grupoCatalogoRepository.save(grupo);

            return GrupoCatalogoMapper.toResponse(
                    grupoActualizado
            );

        } catch (DataIntegrityViolationException exception) {

            throw new GrupoCatalogoDuplicadoException(
                    "Ya existe un grupo de catálogo con ese nombre"
            );
        }
    }
    @Override
        public GrupoCatalogoResponse cambiarEstado(
                Integer id,
                Boolean activo
        ) {

        GrupoCatalogo grupo =
                buscarEntidadPorId(id);

        grupo.setActivo(activo);

        GrupoCatalogo grupoActualizado =
                grupoCatalogoRepository.save(grupo);

        return GrupoCatalogoMapper.toResponse(
                grupoActualizado
        );
        }


    private GrupoCatalogo buscarEntidadPorId(
            Integer id
    ) {

        return grupoCatalogoRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new GrupoCatalogoNoEncontradoException(
                                        "No se encontró un grupo de catálogo con el id "
                                                + id
                                )
                );
    }
}