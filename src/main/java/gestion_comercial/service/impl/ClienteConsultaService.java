package gestion_comercial.service.impl;

import gestion_comercial.dto.response.ClienteConsultaResponse;

import gestion_comercial.entity.Cliente;

import gestion_comercial.exception.ClienteNoEncontradoException;

import gestion_comercial.repository.ClienteRepository;

import gestion_comercial.service.interfaces.IClienteConsultaService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ClienteConsultaService
        implements IClienteConsultaService {


    private final ClienteRepository
            clienteRepository;


    @Override
    @Transactional(readOnly = true)
    public List<ClienteConsultaResponse>
    listar(
            String busqueda
    ) {

        List<Cliente> clientes;


        

        if (
                busqueda == null
        ) {

            clientes =
                    clienteRepository
                            .findAllByOrderByNombreAscApellidoAsc();


            return convertirLista(
                    clientes
            );
        }



        if (
                busqueda.isEmpty()
        ) {

            clientes =
                    clienteRepository
                            .findAllByOrderByNombreAscApellidoAsc();


            return convertirLista(
                    clientes
            );
        }



        if (
                busqueda.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La búsqueda no puede contener solamente espacios"
            );
        }


        String normalizada =
                busqueda
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );


        if (
                !normalizada.matches(
                        ".*[\\p{L}\\p{N}].*"
                )
        ) {

            throw new IllegalArgumentException(
                    "La búsqueda ingresada no es válida"
            );
        }


        clientes =
                clienteRepository
                        .buscarClientes(
                                normalizada
                        );


        return convertirLista(
                clientes
        );
    }


    @Override
    @Transactional(readOnly = true)
    public ClienteConsultaResponse
    buscarPorId(
            Integer idCliente
    ) {

        if (
                idCliente == null
                ||
                idCliente <= 0
        ) {

            throw new ClienteNoEncontradoException(
                    "El cliente indicado no existe"
            );
        }


        Cliente cliente =
                clienteRepository
                        .findById(
                                idCliente
                        )
                        .orElseThrow(
                                () ->
                                        new ClienteNoEncontradoException(
                                                "El cliente indicado no existe"
                                        )
                        );


        return convertirResponse(
                cliente
        );
    }



    private List<ClienteConsultaResponse>
    convertirLista(
            List<Cliente> clientes
    ) {

        return clientes
                .stream()
                .map(
                        this::convertirResponse
                )
                .toList();
    }


    private ClienteConsultaResponse
    convertirResponse(
            Cliente cliente
    ) {

        return new ClienteConsultaResponse(

                cliente.getIdCliente(),

                cliente.getNombre(),

                cliente.getApellido(),

                cliente.getDni(),

                cliente.getTelefono(),

                cliente.getCorreo(),

                cliente.getEstado(),

                cliente.getRazonSocial(),

                cliente.getCuit(),

                cliente.getCondicionIva(),

                cliente.getDireccionFacturacion(),

                cliente.getLocalidadFacturacion(),

                cliente.getProvinciaFacturacion(),

                cliente.getDescuentoPorcentaje(),

                cliente.getNotaInterna(),

                cliente.getFechaAlta(),

                cliente.getUsuario() == null
                        ? null
                        : cliente.getUsuario().getIdUsuario(),

                cliente.getUsuario() == null
                        ? null
                        : cliente.getUsuario().getNombre(),

                cliente.getUsuario() == null
                        ? null
                        : cliente.getUsuario().getApellido(),

                cliente.getUsuario() == null
                        ? null
                        : cliente.getUsuario().getEmail()
        );
    }
}
