package gestion_comercial.service.impl;

import gestion_comercial.dto.request.GrupoCatalogoCreateRequest;
import gestion_comercial.dto.request.GrupoCatalogoUpdateRequest;
import gestion_comercial.dto.response.GrupoCatalogoResponse;
import gestion_comercial.entity.GrupoCatalogo;
import gestion_comercial.exception.GrupoCatalogoNoEncontradoException;
import gestion_comercial.mapper.GrupoCatalogoMapper;
import gestion_comercial.repository.GrupoCatalogoRepository;
import gestion_comercial.service.interfaces.IGrupoCatalogoService;

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

        GrupoCatalogo grupo =
                GrupoCatalogoMapper.toEntity(request);

        GrupoCatalogo grupoGuardado =
                grupoCatalogoRepository.save(grupo);

        return GrupoCatalogoMapper.toResponse(
                grupoGuardado
        );
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

        GrupoCatalogoMapper.updateEntity(
                grupo,
                request
        );

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