package gestion_comercial.service.impl;

import gestion_comercial.dto.request.MedioPagoRequest;
import gestion_comercial.dto.response.MedioPagoResponse;

import gestion_comercial.entity.MedioPago;

import gestion_comercial.exception.MedioPagoDuplicadoException;
import gestion_comercial.exception.MedioPagoNoEncontradoException;

import gestion_comercial.mapper.MedioPagoMapper;

import gestion_comercial.repository.MedioPagoRepository;

import gestion_comercial.service.interfaces.IMedioPagoService;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class MedioPagoService
        implements IMedioPagoService {

    private final MedioPagoRepository
            medioPagoRepository;


    public MedioPagoService(
            MedioPagoRepository medioPagoRepository
    ) {

        this.medioPagoRepository =
                medioPagoRepository;
    }


    // =========================================
    // CREAR - US59
    // =========================================

    @Override
    @Transactional
    public MedioPagoResponse crear(
            MedioPagoRequest request
    ) {

        String nombre =
                normalizarNombre(
                        request.nombre()
                );


        if (
                medioPagoRepository
                        .existsByNombreIgnoreCase(
                                nombre
                        )
        ) {

            throw new MedioPagoDuplicadoException(
                    "Ya existe un medio de pago con ese nombre"
            );
        }


        MedioPago medioPago =
                new MedioPago();


        medioPago.setNombre(
                nombre
        );


        medioPago.setActivo(
                true
        );


        MedioPago guardado =
                guardar(
                        medioPago
                );


        return MedioPagoMapper.toResponse(
                guardado
        );
    }


    // =========================================
    // BUSCAR POR ID - US59
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public MedioPagoResponse buscarPorId(
            Integer idMedioPago
    ) {

        return MedioPagoMapper.toResponse(
                obtenerEntidad(
                        idMedioPago
                )
        );
    }


    // =========================================
    // LISTAR / BUSCAR - US59
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public List<MedioPagoResponse> buscar(
            String nombre,
            Boolean activo
    ) {

        String nombreNormalizado =
                normalizarBusqueda(
                        nombre
                );


        List<MedioPago> mediosPago;


        if (
                nombreNormalizado == null
                &&
                activo == null
        ) {

            mediosPago =
                    medioPagoRepository
                            .findAllByOrderByNombreAsc();


        } else if (
                nombreNormalizado != null
                &&
                activo == null
        ) {

            mediosPago =
                    medioPagoRepository
                            .findByNombreContainingIgnoreCaseOrderByNombreAsc(
                                    nombreNormalizado
                            );


        } else if (
                nombreNormalizado == null
        ) {

            mediosPago =
                    medioPagoRepository
                            .findByActivoOrderByNombreAsc(
                                    activo
                            );


        } else {

            mediosPago =
                    medioPagoRepository
                            .findByNombreContainingIgnoreCaseAndActivoOrderByNombreAsc(

                                    nombreNormalizado,

                                    activo
                            );
        }


        return mediosPago
                .stream()
                .map(
                        MedioPagoMapper::toResponse
                )
                .toList();
    }


    // =========================================
    // MODIFICAR NOMBRE - US59
    // =========================================

    @Override
    @Transactional
    public MedioPagoResponse modificar(
            Integer idMedioPago,
            MedioPagoRequest request
    ) {

        MedioPago medioPago =
                obtenerEntidad(
                        idMedioPago
                );


        String nombre =
                normalizarNombre(
                        request.nombre()
                );


        if (
                medioPagoRepository
                        .existsByNombreIgnoreCaseAndIdMedioPagoNot(

                                nombre,

                                idMedioPago
                        )
        ) {

            throw new MedioPagoDuplicadoException(
                    "Ya existe otro medio de pago con ese nombre"
            );
        }


        medioPago.setNombre(
                nombre
        );


        MedioPago guardado =
                guardar(
                        medioPago
                );


        return MedioPagoMapper.toResponse(
                guardado
        );
    }


    // =========================================
    // ACTIVAR / DESACTIVAR - US59
    // =========================================

    @Override
    @Transactional
    public MedioPagoResponse cambiarEstado(
            Integer idMedioPago,
            Boolean activo
    ) {

        MedioPago medioPago =
                obtenerEntidad(
                        idMedioPago
                );


        medioPago.setActivo(
                activo
        );


        MedioPago guardado =
                medioPagoRepository.save(
                        medioPago
                );


        return MedioPagoMapper.toResponse(
                guardado
        );
    }


    // =========================================
    // LISTAR ACTIVOS PARA CHECKOUT - US11
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public List<MedioPagoResponse>
    listarActivos() {

        return medioPagoRepository
                .findByActivoOrderByNombreAsc(
                        true
                )
                .stream()
                .map(
                        MedioPagoMapper::toResponse
                )
                .toList();
    }


    // =========================================
    // VALIDAR MEDIO ACTIVO - US11
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public MedioPagoResponse buscarActivoPorId(
            Integer idMedioPago
    ) {

        if (
                idMedioPago == null
                ||
                idMedioPago <= 0
        ) {

            throw new MedioPagoNoEncontradoException(
                    "El medio de pago seleccionado no existe o no está activo"
            );
        }


        MedioPago medioPago =
                medioPagoRepository
                        .findByIdMedioPagoAndActivoTrue(
                                idMedioPago
                        )
                        .orElseThrow(
                                () ->
                                        new MedioPagoNoEncontradoException(
                                                "El medio de pago seleccionado no existe o no está activo"
                                        )
                        );


        return MedioPagoMapper.toResponse(
                medioPago
        );
    }


    // =========================================
    // OBTENER ENTIDAD
    // =========================================

    private MedioPago obtenerEntidad(
            Integer idMedioPago
    ) {

        if (
                idMedioPago == null
                ||
                idMedioPago <= 0
        ) {

            throw new MedioPagoNoEncontradoException(
                    "No existe un medio de pago con ese ID"
            );
        }


        return medioPagoRepository
                .findById(
                        idMedioPago
                )
                .orElseThrow(
                        () ->
                                new MedioPagoNoEncontradoException(
                                        "No existe un medio de pago con ese ID"
                                )
                );
    }


    // =========================================
    // NORMALIZAR NOMBRE
    // =========================================

    private String normalizarNombre(
            String nombre
    ) {

        return nombre
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }


    // =========================================
    // NORMALIZAR BÚSQUEDA
    // =========================================

    private String normalizarBusqueda(
            String nombre
    ) {

        if (
                nombre == null
        ) {

            return null;
        }


        if (
                nombre.isEmpty()
        ) {

            return null;
        }


        String nombreNormalizado =
                nombre
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        if (
                nombreNormalizado.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "La búsqueda no puede contener solo espacios"
            );
        }


        if (
                nombreNormalizado.length() > 80
        ) {

            throw new IllegalArgumentException(
                    "La búsqueda no puede superar los 80 caracteres"
            );
        }


        if (
                !nombreNormalizado.matches(
                        ".*\\p{L}.*"
                )
        ) {

            throw new IllegalArgumentException(
                    "La búsqueda debe contener al menos una letra"
            );
        }


        return nombreNormalizado;
    }


    // =========================================
    // GUARDAR CON PROTECCIÓN DE DUPLICADOS
    // =========================================

    private MedioPago guardar(
            MedioPago medioPago
    ) {

        try {

            return medioPagoRepository
                    .saveAndFlush(
                            medioPago
                    );


        } catch (
                DataIntegrityViolationException exception
        ) {

            throw new MedioPagoDuplicadoException(
                    "Ya existe un medio de pago con ese nombre"
            );
        }
    }
}