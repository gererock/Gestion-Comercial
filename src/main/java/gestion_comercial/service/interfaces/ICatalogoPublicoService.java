package gestion_comercial.service.interfaces;

import gestion_comercial.dto.response.CatalogoItemResponse;

import java.util.List;


public interface ICatalogoPublicoService {

    List<CatalogoItemResponse> listar();

}