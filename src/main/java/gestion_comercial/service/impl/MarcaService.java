package gestion_comercial.service.impl;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import gestion_comercial.dto.request.MarcaCreateRequest;
import gestion_comercial.dto.request.MarcaUpdateRequest;
import gestion_comercial.dto.response.MarcaResponse;
import gestion_comercial.entity.Marca;
import gestion_comercial.exception.MarcaDuplicadaException;
import gestion_comercial.exception.MarcaNoEncontradaException;
import gestion_comercial.mapper.MarcaMapper;
import gestion_comercial.repository.MarcaRespository;
import gestion_comercial.service.interfaces.IMarcaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarcaService implements IMarcaService {
    private final MarcaRespository marcaRespository;

    @Override
    public MarcaResponse crear(MarcaCreateRequest request) {
        String nombre = request.nombre().trim();

        if (marcaRespository.existsByNombreIgnoreCase(nombre)) {
            throw new MarcaDuplicadaException("Ya existe una marca con este nombre");
        }
        Marca marca = MarcaMapper.toEntity(request);

        Marca marcaGuardar = marcaRespository.save(marca);

        return MarcaMapper.toResponse(marcaGuardar);
    }

    @Override
    public List<MarcaResponse> obtenerTodo() {

        List<Marca> marcas = marcaRespository.findAll();

        return marcas
                .stream()
                .map(MarcaMapper::toResponse)
                .toList();

    }

    @Override
    public MarcaResponse actualizar(
            Integer id,
            MarcaUpdateRequest request) {

        Marca marca = marcaRespository.findById(id)
                .orElseThrow(() -> new MarcaNoEncontradaException(
                        "No se encontró una marca con este id"));

        String nombre = request.nombre().trim();

        if (marcaRespository
                .existsByNombreIgnoreCaseAndIdNot(nombre, id)) {

            throw new MarcaDuplicadaException(
                    "Ya existe una marca con este nombre");
        }

        marca.setNombre(nombre);

        Marca marcaActualizada = marcaRespository.save(marca);

        return MarcaMapper.toResponse(
                marcaActualizada);
    }

    @Override
    public MarcaResponse cambiarEstado(Integer id, Boolean activa) {
        Marca marca = marcaRespository.findById(id)
                .orElseThrow(() -> new MarcaNoEncontradaException("No se encontro una marca con este id"));

        marca.setActiva(activa);

        Marca marcaActualizada = marcaRespository.save(marca);

        return MarcaMapper.toResponse(marcaActualizada);
    }

    @Override
    public List<MarcaResponse> buscar(
            String nombre,
            Boolean activa) {

        if (nombre != null) {

            if (!nombre.isEmpty()
                    && nombre.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "El nombre de búsqueda no puede contener solo espacios");
            }

            if (!nombre.isEmpty()
                    && !nombre.matches(".*\\p{L}.*")) {

                throw new IllegalArgumentException(
                        "El nombre de búsqueda debe contener al menos una letra");
            }
        }

        List<Marca> marcas;

        if (nombre == null && activa == null) {

            marcas = marcaRespository.findAll();

        } else if (nombre != null && activa == null) {

            marcas = marcaRespository
                    .findByNombreContainingIgnoreCase(
                            nombre.trim());

        } else if (nombre == null) {

            marcas = marcaRespository
                    .findByActiva(activa);

        } else {

            marcas = marcaRespository
                    .findByNombreContainingIgnoreCaseAndActiva(
                            nombre.trim(),
                            activa);
        }

        return marcas
                .stream()
                .map(MarcaMapper::toResponse)
                .toList();
    }

}
