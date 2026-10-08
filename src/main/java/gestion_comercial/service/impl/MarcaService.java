package gestion_comercial.service.impl;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import gestion_comercial.dto.request.MarcaCreateRequest;
import gestion_comercial.dto.response.MarcaResponse;
import gestion_comercial.entity.Marca;
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
}
