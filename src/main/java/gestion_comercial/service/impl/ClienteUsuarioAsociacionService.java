package gestion_comercial.service.impl;

import gestion_comercial.dto.request.ClienteUsuarioAsociacionRequest;
import gestion_comercial.dto.response.ClienteAsociacionResponse;
import gestion_comercial.dto.response.ClienteUsuarioAsociacionResponse;
import gestion_comercial.dto.response.UsuarioAsociacionResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.EstadoUsuario;
import gestion_comercial.entity.Usuario;
import gestion_comercial.exception.ClienteUsuarioAsociacionException;
import gestion_comercial.repository.ClienteRepository;
import gestion_comercial.repository.UsuarioRepository;
import gestion_comercial.service.interfaces.IClienteUsuarioAsociacionService;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteUsuarioAsociacionService
        implements IClienteUsuarioAsociacionService {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteAsociacionResponse> buscarClientes(
            String busqueda
    ) {
        String normalizada = validarBusqueda(busqueda);

        List<Cliente> clientes = normalizada == null
                ? clienteRepository.findAllByOrderByNombreAscApellidoAsc()
                : clienteRepository.buscarClientes(normalizada);

        return clientes.stream()
                .map(this::convertirCliente)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioAsociacionResponse> buscarUsuarios(
            String busqueda
    ) {
        String normalizada = validarBusqueda(busqueda);

        List<Usuario> usuarios = normalizada == null
                ? usuarioRepository.findAllByOrderByNombreAscApellidoAsc()
                : usuarioRepository.buscarUsuarios(normalizada);

        return usuarios.stream()
                .map(this::convertirUsuario)
                .toList();
    }

    @Override
    @Transactional
    public ClienteUsuarioAsociacionResponse asociar(
            ClienteUsuarioAsociacionRequest request
    ) {
        Cliente cliente = clienteRepository
                .buscarPorIdParaAsociar(request.idCliente())
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "El cliente seleccionado no existe"
                ));

        Usuario usuario = usuarioRepository
                .buscarPorIdParaAsociar(request.idUsuario())
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "El usuario seleccionado no existe"
                ));

        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "No se puede asociar un cliente inactivo"
            );
        }

        if (cliente.getUsuario() != null) {
            throw error(
                    HttpStatus.CONFLICT,
                    "El cliente ya está asociado a un usuario"
            );
        }

        if (usuario.getRol() == null
                || !ROL_CLIENTE.equalsIgnoreCase(usuario.getRol().getNombre())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Solo se pueden asociar usuarios con rol cliente"
            );
        }

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw error(
                    HttpStatus.CONFLICT,
                    "No se puede asociar un usuario inactivo"
            );
        }

        if (clienteRepository.existsByUsuario_IdUsuario(usuario.getIdUsuario())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "El usuario ya está asociado a otro cliente"
            );
        }

        cliente.setUsuario(usuario);

        try {
            clienteRepository.saveAndFlush(cliente);
        } catch (DataIntegrityViolationException exception) {
            throw error(
                    HttpStatus.CONFLICT,
                    "El cliente o el usuario ya poseen una asociación"
            );
        }

        return new ClienteUsuarioAsociacionResponse(
                cliente.getIdCliente(),
                cliente.getNombre(),
                cliente.getApellido(),
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                "Cliente y usuario asociados correctamente"
        );
    }

    private String validarBusqueda(
            String busqueda
    ) {
        if (busqueda == null || busqueda.isEmpty()) {
            return null;
        }

        if (busqueda.isBlank()) {
            throw new IllegalArgumentException(
                    "La búsqueda no puede contener solamente espacios"
            );
        }

        String normalizada = busqueda.trim().replaceAll("\\s+", " ");

        if (normalizada.length() > 120) {
            throw new IllegalArgumentException(
                    "La búsqueda no puede superar los 120 caracteres"
            );
        }

        if (!normalizada.matches(".*[\\p{L}\\p{N}].*")) {
            throw new IllegalArgumentException(
                    "La búsqueda ingresada no es válida"
            );
        }

        return normalizada;
    }

    private ClienteAsociacionResponse convertirCliente(
            Cliente cliente
    ) {
        Usuario asociado = cliente.getUsuario();

        return new ClienteAsociacionResponse(
                cliente.getIdCliente(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDni(),
                cliente.getCorreo(),
                cliente.getEstado(),
                asociado != null,
                asociado == null ? null : asociado.getIdUsuario(),
                asociado == null ? null : asociado.getEmail()
        );
    }

    private UsuarioAsociacionResponse convertirUsuario(
            Usuario usuario
    ) {
        Cliente clienteAsociado = clienteRepository
                .findByUsuario_EmailIgnoreCase(usuario.getEmail())
                .orElse(null);

        return new UsuarioAsociacionResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol() == null ? null : usuario.getRol().getNombre(),
                usuario.getEstado() == null ? null : usuario.getEstado().name(),
                clienteAsociado != null,
                clienteAsociado == null ? null : clienteAsociado.getIdCliente()
        );
    }

    private ClienteUsuarioAsociacionException error(
            HttpStatus status,
            String mensaje
    ) {
        return new ClienteUsuarioAsociacionException(status, mensaje);
    }
}
