package gestion_comercial.service.impl;

import gestion_comercial.dto.request.NotaInternaClienteRequest;
import gestion_comercial.dto.response.NotaInternaClienteResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.exception.ClienteNoEncontradoException;
import gestion_comercial.repository.ClienteRepository;
import gestion_comercial.service.interfaces.INotaInternaClienteService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotaInternaClienteService
        implements INotaInternaClienteService {

    private static final int LONGITUD_MAXIMA = 1000;

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public NotaInternaClienteResponse actualizar(
            Integer idCliente,
            NotaInternaClienteRequest request
    ) {
        if (idCliente == null || idCliente <= 0) {
            throw new ClienteNoEncontradoException(
                    "El cliente indicado no existe"
            );
        }

        Cliente cliente = clienteRepository
                .buscarPorIdParaModificarNotaInterna(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException(
                        "El cliente indicado no existe"
                ));

        String nota = normalizarNota(request.notaInterna());

        cliente.setNotaInterna(nota);
        Cliente guardado = clienteRepository.save(cliente);

        return new NotaInternaClienteResponse(
                guardado.getIdCliente(),
                guardado.getNombre(),
                guardado.getApellido(),
                guardado.getNotaInterna(),
                nota == null
                        ? "Nota interna eliminada correctamente"
                        : "Nota interna actualizada correctamente"
        );
    }

    private String normalizarNota(String valor) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }

        if (valor.isBlank()) {
            throw new IllegalArgumentException(
                    "La nota interna no puede contener solamente espacios en blanco"
            );
        }

        String nota = valor.trim();

        if (nota.length() > LONGITUD_MAXIMA) {
            throw new IllegalArgumentException(
                    "La nota interna no puede superar los 1000 caracteres"
            );
        }

        boolean tieneContenido = nota.codePoints()
                .anyMatch(Character::isLetterOrDigit);

        if (!tieneContenido) {
            throw new IllegalArgumentException(
                    "La nota interna debe contener al menos una letra o un número"
            );
        }

        return nota;
    }
}
