package gestion_comercial.service.impl;

import gestion_comercial.dto.request.GastoCreateRequest;
import gestion_comercial.dto.response.GastoResponse;
import gestion_comercial.entity.CategoriaGasto;
import gestion_comercial.entity.Gasto;
import gestion_comercial.entity.MedioPago;
import gestion_comercial.entity.Usuario;
import gestion_comercial.exception.GastoException;
import gestion_comercial.mapper.GastoMapper;
import gestion_comercial.repository.CategoriaGastoRepository;
import gestion_comercial.repository.GastoRepository;
import gestion_comercial.repository.MedioPagoRepository;
import gestion_comercial.repository.UsuarioRepository;
import gestion_comercial.service.interfaces.IGastoService;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class GastoService
        implements IGastoService {

    private static final String ROL_ADMINISTRADOR =
            "ADMINISTRADOR";


    private final GastoRepository
            gastoRepository;

    private final CategoriaGastoRepository
            categoriaGastoRepository;

    private final MedioPagoRepository
            medioPagoRepository;

    private final UsuarioRepository
            usuarioRepository;


    @Override
    @Transactional
    public GastoResponse crear(
            GastoCreateRequest request,
            String emailUsuario
    ) {

        Usuario usuario =
                obtenerAdministrador(
                        emailUsuario
                );


        CategoriaGasto categoria =
                obtenerCategoriaActiva(
                        request.idCategoriaGasto()
                );


        MedioPago medioPago =
                obtenerMedioPagoActivo(
                        request.idMedioPago()
                );


        String descripcion =
                request.descripcion().trim();


        String observaciones =
                normalizarObservaciones(
                        request.observaciones()
                );


        String claveOperacion =
                request.claveOperacion().trim();


        Gasto gastoExistente =
                gastoRepository
                        .findByClaveOperacion(
                                claveOperacion
                        )
                        .orElse(null);


        if (
                gastoExistente != null
        ) {

            return resolverOperacionRepetida(
                    gastoExistente,
                    categoria,
                    usuario,
                    medioPago,
                    descripcion,
                    request.monto(),
                    request.fecha(),
                    observaciones
            );
        }


        Gasto gasto =
                Gasto.builder()

                        .categoriaGasto(
                                categoria
                        )

                        .usuario(
                                usuario
                        )

                        .medioPago(
                                medioPago
                        )

                        .descripcion(
                                descripcion
                        )

                        .monto(
                                request.monto()
                        )

                        .fecha(
                                request.fecha()
                        )

                        .observaciones(
                                observaciones
                        )

                        .claveOperacion(
                                claveOperacion
                        )

                        .build();


        try {

            Gasto guardado =
                    gastoRepository
                            .saveAndFlush(
                                    gasto
                            );


            return GastoMapper.toResponse(
                    guardado
            );

        } catch (
                DataIntegrityViolationException exception
        ) {

            throw error(
                    HttpStatus.CONFLICT,
                    "El gasto ya fue registrado"
            );
        }
    }


    private Usuario obtenerAdministrador(
            String emailUsuario
    ) {

        if (
                emailUsuario == null
                ||
                emailUsuario.isBlank()
        ) {

            throw error(
                    HttpStatus.UNAUTHORIZED,
                    "No se pudo identificar al usuario que registra el gasto"
            );
        }


        Usuario usuario =
                usuarioRepository
                        .findByEmailIgnoreCase(
                                emailUsuario
                        )
                        .orElseThrow(
                                () -> error(
                                        HttpStatus.UNAUTHORIZED,
                                        "No se pudo identificar al usuario que registra el gasto"
                                )
                        );


        if (
                usuario.getRol() == null
                ||
                !ROL_ADMINISTRADOR.equals(
                        usuario.getRol().getNombre()
                )
        ) {

            throw error(
                    HttpStatus.FORBIDDEN,
                    "Solo un administrador puede registrar gastos"
            );
        }


        return usuario;
    }


    private CategoriaGasto obtenerCategoriaActiva(
            Integer idCategoriaGasto
    ) {

        CategoriaGasto categoria =
                categoriaGastoRepository
                        .findById(
                                idCategoriaGasto
                        )
                        .orElseThrow(
                                () -> error(
                                        HttpStatus.NOT_FOUND,
                                        "La categoría de gasto indicada no existe"
                                )
                        );


        if (
                !Boolean.TRUE.equals(
                        categoria.getActiva()
                )
        ) {

            throw error(
                    HttpStatus.CONFLICT,
                    "La categoría de gasto indicada está inactiva"
            );
        }


        return categoria;
    }


    private MedioPago obtenerMedioPagoActivo(
            Integer idMedioPago
    ) {

        if (
                idMedioPago == null
        ) {

            return null;
        }


        MedioPago medioPago =
                medioPagoRepository
                        .findById(
                                idMedioPago
                        )
                        .orElseThrow(
                                () -> error(
                                        HttpStatus.NOT_FOUND,
                                        "El medio de pago indicado no existe"
                                )
                        );


        if (
                !Boolean.TRUE.equals(
                        medioPago.getActivo()
                )
        ) {

            throw error(
                    HttpStatus.CONFLICT,
                    "El medio de pago indicado está inactivo"
            );
        }


        return medioPago;
    }


    private String normalizarObservaciones(
            String observaciones
    ) {

        if (
                observaciones == null
        ) {

            return null;
        }


        String valor =
                observaciones.trim();


        return valor.isEmpty()
                ? null
                : valor;
    }


    private GastoResponse resolverOperacionRepetida(

            Gasto gastoExistente,

            CategoriaGasto categoria,

            Usuario usuario,

            MedioPago medioPago,

            String descripcion,

            BigDecimal monto,

            LocalDate fecha,

            String observaciones
    ) {

        boolean mismaCategoria =
                gastoExistente
                        .getCategoriaGasto()
                        .getIdCategoriaGasto()
                        .equals(
                                categoria.getIdCategoriaGasto()
                        );


        boolean mismoUsuario =
                gastoExistente
                        .getUsuario()
                        .getIdUsuario()
                        .equals(
                                usuario.getIdUsuario()
                        );


        boolean mismoMedioPago =
                mismosMediosPago(
                        gastoExistente.getMedioPago(),
                        medioPago
                );


        boolean mismaDescripcion =
                gastoExistente
                        .getDescripcion()
                        .equals(
                                descripcion
                        );


        boolean mismoMonto =
                gastoExistente
                        .getMonto()
                        .compareTo(
                                monto
                        ) == 0;


        boolean mismaFecha =
                gastoExistente
                        .getFecha()
                        .equals(
                                fecha
                        );


        boolean mismasObservaciones =
                Objects.equals(
                        gastoExistente.getObservaciones(),
                        observaciones
                );


        if (
                !mismaCategoria
                ||
                !mismoUsuario
                ||
                !mismoMedioPago
                ||
                !mismaDescripcion
                ||
                !mismoMonto
                ||
                !mismaFecha
                ||
                !mismasObservaciones
        ) {

            throw error(
                    HttpStatus.CONFLICT,
                    "La identificación de la operación ya fue utilizada"
            );
        }


        return GastoMapper.toResponse(
                gastoExistente
        );
    }


    private boolean mismosMediosPago(
            MedioPago primero,
            MedioPago segundo
    ) {

        if (
                primero == null
                &&
                segundo == null
        ) {

            return true;
        }


        if (
                primero == null
                ||
                segundo == null
        ) {

            return false;
        }


        return primero
                .getIdMedioPago()
                .equals(
                        segundo.getIdMedioPago()
                );
    }


    private GastoException error(
            HttpStatus status,
            String mensaje
    ) {

        return new GastoException(
                status,
                mensaje
        );
    }
}