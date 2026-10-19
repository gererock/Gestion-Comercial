package gestion_comercial.service.impl;

import gestion_comercial.dto.request.DireccionEnvioRequest;
import gestion_comercial.dto.response.ConfiguracionCheckoutEnvioResponse;
import gestion_comercial.dto.response.DireccionEnvioResponse;

import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.DireccionEnvioCliente;
import gestion_comercial.entity.LocalidadEnvio;

import gestion_comercial.repository.ClienteRepository;
import gestion_comercial.repository.DireccionEnvioRepository;

import gestion_comercial.service.interfaces.IConfiguracionComercioService;
import gestion_comercial.service.interfaces.IDireccionEnvioService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DireccionEnvioService
        implements IDireccionEnvioService {

    private final DireccionEnvioRepository
            direccionEnvioRepository;

    private final ClienteRepository
            clienteRepository;

    private final IConfiguracionComercioService
            configuracionComercioService;




    @Override
    @Transactional(readOnly = true)
    public List<DireccionEnvioResponse> listar(
            String emailUsuario
    ) {

        Cliente cliente =
                obtenerClienteAutenticado(
                        emailUsuario
                );


        ConfiguracionCheckoutEnvioResponse configuracion =
                configuracionComercioService
                        .obtenerCheckout();


        return direccionEnvioRepository
                .findByCliente_IdClienteOrderByIdDireccionEnvioDesc(
                        cliente.getIdCliente()
                )
                .stream()
                .map(
                        direccion ->
                                convertirResponse(
                                        direccion,
                                        configuracion
                                )
                )
                .toList();
    }

    @Override
    @Transactional
    public DireccionEnvioResponse registrar(
            String emailUsuario,
            DireccionEnvioRequest request
    ) {

        Cliente cliente =
                obtenerClienteAutenticado(
                        emailUsuario
                );


        String calle =
                validarCalle(
                        request.calle()
                );


        String numero =
                validarNumero(
                        request.numero()
                );


        String pisoDepartamento =
                validarPisoDepartamento(
                        request.pisoDepartamento()
                );


        String referencia =
                normalizarOpcional(
                        request.referencia()
                );


        ConfiguracionCheckoutEnvioResponse configuracion =
                configuracionComercioService
                        .obtenerCheckout();


        validarLocalidadHabilitada(
                request.localidad(),
                configuracion
        );


        DireccionEnvioCliente direccion =
                new DireccionEnvioCliente();


        direccion.setCliente(
                cliente
        );

        direccion.setCalle(
                calle
        );

        direccion.setNumero(
                numero
        );

        direccion.setPisoDepartamento(
                pisoDepartamento
        );

        direccion.setLocalidad(
                request.localidad()
        );

        direccion.setReferencia(
                referencia
        );


        DireccionEnvioCliente guardada =
                direccionEnvioRepository
                        .save(
                                direccion
                        );


        return convertirResponse(
                guardada,
                configuracion
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DireccionEnvioResponse buscarPropia(
            String emailUsuario,
            Integer idDireccionEnvio
    ) {

        Cliente cliente =
                obtenerClienteAutenticado(
                        emailUsuario
                );


        if (
                idDireccionEnvio == null
                ||
                idDireccionEnvio <= 0
        ) {

            throw new IllegalArgumentException(
                    "La dirección seleccionada no es válida"
            );
        }


        DireccionEnvioCliente direccion =
                direccionEnvioRepository
                        .findByIdDireccionEnvioAndCliente_IdCliente(
                                idDireccionEnvio,
                                cliente.getIdCliente()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "La dirección seleccionada no pertenece al cliente autenticado"
                                        )
                        );


        ConfiguracionCheckoutEnvioResponse configuracion =
                configuracionComercioService
                        .obtenerCheckout();


        validarLocalidadHabilitada(
                direccion.getLocalidad(),
                configuracion
        );


        return convertirResponse(
                direccion,
                configuracion
        );
    }


    private Cliente obtenerClienteAutenticado(
            String emailUsuario
    ) {

        if (
                emailUsuario == null
                ||
                emailUsuario.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "No se pudo identificar al cliente autenticado"
            );
        }


        return clienteRepository
                .findByUsuario_EmailIgnoreCase(
                        emailUsuario
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "No se encontró un cliente asociado al usuario autenticado"
                                )
                );
    }


    private String validarCalle(
            String calle
    ) {

        String calleNormalizada =
                calle
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        if (
                !calleNormalizada.matches(
                        ".*\\p{L}.*"
                )
        ) {

            throw new IllegalArgumentException(
                    "La calle debe contener al menos una letra"
            );
        }


        return calleNormalizada;
    }



    private String validarNumero(
            String numero
    ) {

        String numeroNormalizado =
                numero
                        .trim()
                        .toUpperCase();


        if (
                !numeroNormalizado.matches(
                        "(?:[1-9]\\d*|S/N)"
                )
        ) {

            throw new IllegalArgumentException(
                    "El número debe ser positivo o S/N"
            );
        }


        return numeroNormalizado;
    }


    private String validarPisoDepartamento(
            String pisoDepartamento
    ) {

        String valor =
                normalizarOpcional(
                        pisoDepartamento
                );


        if (
                valor == null
        ) {

            return null;
        }


        if (
                !valor.matches(
                        ".*[\\p{L}\\p{N}].*"
                )
        ) {

            throw new IllegalArgumentException(
                    "El piso o departamento contiene un formato inválido"
            );
        }


        return valor;
    }


    private void validarLocalidadHabilitada(
            LocalidadEnvio localidad,
            ConfiguracionCheckoutEnvioResponse configuracion
    ) {

        if (
                localidad == null
        ) {

            throw new IllegalArgumentException(
                    "La localidad es obligatoria"
            );
        }


        boolean habilitada =
                switch (
                        localidad
                ) {

                    case VILLA_MARIA ->
                            Boolean.TRUE.equals(
                                    configuracion
                                            .permiteEnvioVillaMaria()
                            );


                    case VILLA_NUEVA ->
                            Boolean.TRUE.equals(
                                    configuracion
                                            .permiteEnvioVillaNueva()
                            );
                };


        if (
                !habilitada
        ) {

            throw new IllegalArgumentException(
                    "La localidad seleccionada no está habilitada para envío"
            );
        }
    }

    private String normalizarOpcional(
            String valor
    ) {

        if (
                valor == null
        ) {

            return null;
        }


        String valorNormalizado =
                valor
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        if (
                valorNormalizado.isEmpty()
        ) {

            return null;
        }


        return valorNormalizado;
    }


    private DireccionEnvioResponse convertirResponse(
            DireccionEnvioCliente direccion,
            ConfiguracionCheckoutEnvioResponse configuracion
    ) {

        boolean habilitada =
                switch (
                        direccion.getLocalidad()
                ) {

                    case VILLA_MARIA ->
                            Boolean.TRUE.equals(
                                    configuracion
                                            .permiteEnvioVillaMaria()
                            );


                    case VILLA_NUEVA ->
                            Boolean.TRUE.equals(
                                    configuracion
                                            .permiteEnvioVillaNueva()
                            );
                };


        return new DireccionEnvioResponse(

                direccion.getIdDireccionEnvio(),

                direccion.getCalle(),

                direccion.getNumero(),

                direccion.getPisoDepartamento(),

                direccion.getLocalidad(),

                direccion.getReferencia(),

                habilitada
        );
    }
}