package gestion_comercial.service.impl;

import gestion_comercial.dto.request.ClienteActualizacionRequest;
import gestion_comercial.dto.response.ClienteActualizacionResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.exception.ClienteNoEncontradoException;
import gestion_comercial.repository.ClienteRepository;
import gestion_comercial.service.interfaces.IClienteActualizacionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteActualizacionService
        implements IClienteActualizacionService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public ClienteActualizacionResponse actualizar(
            Integer idCliente,
            ClienteActualizacionRequest request
    ) {

        if (idCliente == null || idCliente <= 0) {
            throw new ClienteNoEncontradoException(
                    "El cliente indicado no existe"
            );
        }

        Cliente cliente = clienteRepository
                .findById(idCliente)
                .orElseThrow(
                        () -> new ClienteNoEncontradoException(
                                "El cliente indicado no existe"
                        )
                );

        cliente.setNombre(request.nombre().trim());
        cliente.setApellido(limpiarOpcional(request.apellido()));
        cliente.setDni(limpiarOpcional(request.dni()));
        cliente.setTelefono(limpiarOpcional(request.telefono()));
        cliente.setCorreo(limpiarOpcional(request.correo()));
        cliente.setRazonSocial(limpiarOpcional(request.razonSocial()));
        cliente.setCuit(limpiarOpcional(request.cuit()));
        cliente.setCondicionIva(limpiarOpcional(request.condicionIva()));
        cliente.setDireccionFacturacion(
                limpiarOpcional(request.direccionFacturacion())
        );
        cliente.setLocalidadFacturacion(
                limpiarOpcional(request.localidadFacturacion())
        );
        cliente.setProvinciaFacturacion(
                limpiarOpcional(request.provinciaFacturacion())
        );
        cliente.setDescuentoPorcentaje(request.descuentoPorcentaje());
        cliente.setNotaInterna(limpiarOpcional(request.notaInterna()));
        cliente.setEstado(request.estado());

        Cliente guardado = clienteRepository.save(cliente);

        return convertirResponse(guardado);
    }

    private ClienteActualizacionResponse convertirResponse(
            Cliente cliente
    ) {

        return new ClienteActualizacionResponse(
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
                "Cliente actualizado correctamente"
        );
    }

    private String limpiarOpcional(
            String valor
    ) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
