package gestion_comercial.service.impl;

import gestion_comercial.dto.request.ClienteCreacionRequest;
import gestion_comercial.dto.response.ClienteCreacionResponse;

import gestion_comercial.entity.Cliente;

import gestion_comercial.repository.ClienteRepository;

import gestion_comercial.service.interfaces.IClienteCreacionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ClienteCreacionService
        implements IClienteCreacionService {


    private final ClienteRepository clienteRepository;


    @Override
    @Transactional
    public ClienteCreacionResponse crear(
            ClienteCreacionRequest request
    ) {

        Cliente cliente =
                new Cliente();


        cliente.setNombre(
                request.nombre().trim()
        );


        cliente.setApellido(
                limpiarOpcional(
                        request.apellido()
                )
        );


        cliente.setDni(
                limpiarOpcional(
                        request.dni()
                )
        );


        cliente.setTelefono(
                limpiarOpcional(
                        request.telefono()
                )
        );


        cliente.setCorreo(
                limpiarOpcional(
                        request.correo()
                )
        );


        cliente.setRazonSocial(
                limpiarOpcional(
                        request.razonSocial()
                )
        );


        cliente.setCuit(
                limpiarOpcional(
                        request.cuit()
                )
        );


        cliente.setCondicionIva(
                limpiarOpcional(
                        request.condicionIva()
                )
        );


        cliente.setDireccionFacturacion(
                limpiarOpcional(
                        request.direccionFacturacion()
                )
        );


        cliente.setLocalidadFacturacion(
                limpiarOpcional(
                        request.localidadFacturacion()
                )
        );


        cliente.setProvinciaFacturacion(
                limpiarOpcional(
                        request.provinciaFacturacion()
                )
        );


        cliente.setDescuentoPorcentaje(
                request.descuentoPorcentaje()
        );


        cliente.setNotaInterna(
                limpiarOpcional(
                        request.notaInterna()
                )
        );


      

        cliente.setEstado(
                request.estado() == null
                        ? true
                        : request.estado()
        );




        Cliente guardado =
                clienteRepository.save(
                        cliente
                );


        return new ClienteCreacionResponse(

                guardado.getIdCliente(),

                guardado.getNombre(),

                guardado.getApellido(),

                guardado.getEstado(),

                guardado.getFechaAlta(),

                "Cliente creado correctamente"
        );
    }


    private String limpiarOpcional(
            String valor
    ) {

        if (
                valor == null
                ||
                valor.isBlank()
        ) {

            return null;
        }


        return valor.trim();
    }
}