package gestion_comercial.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import gestion_comercial.dto.request.CategoriaCreateRequest;
import gestion_comercial.dto.request.CategoriaUpdateRequest;
import gestion_comercial.dto.response.CategoriaResponse;
import gestion_comercial.entity.Categoria;
import gestion_comercial.exception.CategoriaDuplicadaException;
import gestion_comercial.exception.CategoriaNoEncontradaException;
import gestion_comercial.mapper.CategoriaMapper;
import gestion_comercial.repository.CategoriaRespository;
import gestion_comercial.service.interfaces.ICategoriaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService implements ICategoriaService {
    private final CategoriaRespository categoriaRespository;

    @Override
    public CategoriaResponse crear(CategoriaCreateRequest request) {

        String nombre = request.nombre().trim();

        if (categoriaRespository.existsByNombreIgnoreCase(nombre)) {
            throw new CategoriaDuplicadaException(
                    "Ya existe una categoria con este nombre");
        }

        Categoria categoria = CategoriaMapper.toEntity(request);

        categoria.setNombre(nombre);

        Categoria categoriaAGuardar = categoriaRespository.save(categoria);

        return CategoriaMapper.toResponse(categoriaAGuardar);
    }

    @Override
    public CategoriaResponse buscarPorId(Integer id) {
        Categoria categoria = categoriaRespository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException("Categoria no encontrada"));

        return CategoriaMapper.toResponse(categoria);
    }

    @Override
    public CategoriaResponse actualizar(Integer id, CategoriaUpdateRequest request) {
        Categoria categoria = categoriaRespository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException("No se encontro una categoria con este id"));

        String nombre = request.nombre().trim();

        if (categoriaRespository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new CategoriaDuplicadaException("Ya existe una categoria con este nombre");
        }

        categoria.setNombre(nombre);
        categoria.setDescripcion(request.descripcion());

        Categoria autualizarCategoria = categoriaRespository.save(categoria);

        return CategoriaMapper.toResponse(autualizarCategoria);
    }

    @Override
    public CategoriaResponse cambiarEstado(Integer id, Boolean activa) {
        Categoria categoria = categoriaRespository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException("No se encontro una categoria con este id"));

        categoria.setActiva(activa);

        Categoria actualizarCategoria = categoriaRespository.save(categoria);

        return CategoriaMapper.toResponse(actualizarCategoria);
    }

    @Override
    public List<CategoriaResponse> buscar(String nombre, Boolean activa) {
    if (nombre != null) {

        if (!nombre.isEmpty() && nombre.trim().isEmpty()) {
        throw new IllegalArgumentException(
                "El nombre de búsqueda no puede contener solo espacios"
            );
        }

        if (!nombre.isEmpty()
            && !nombre.matches(".*\\p{L}.*")) {

            throw new IllegalArgumentException(
                "El nombre de búsqueda debe contener al menos una letra"
            );
        }
    }
        List<Categoria> categorias;

        if (nombre == null && activa == null) {

            categorias = categoriaRespository.findAll();

        }else if (nombre != null && activa == null) {
            categorias = categoriaRespository.findByNombreContainingIgnoreCase(nombre.trim());
        }else if (nombre == null) {
            categorias = categoriaRespository.findByActiva(activa);
        }else {
            categorias = categoriaRespository.findByNombreContainingIgnoreCaseAndActiva(nombre.trim(), activa);
        }

        return categorias
            .stream()
            .map(CategoriaMapper::toResponse)
            .toList();
    }

}
