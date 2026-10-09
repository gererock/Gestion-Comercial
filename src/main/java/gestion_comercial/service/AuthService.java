package gestion_comercial.service;

import gestion_comercial.dto.request.LoginRequest;
import gestion_comercial.dto.request.RegistroRequest;
import gestion_comercial.dto.response.LoginResponse;
import gestion_comercial.dto.response.RegistroResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.entity.EstadoUsuario;
import gestion_comercial.entity.Rol;
import gestion_comercial.entity.Usuario;
import gestion_comercial.exception.EmailYaRegistradoException;
import gestion_comercial.exception.UsuarioNoHabilitadoException;
import gestion_comercial.repository.ClienteRepository;
import gestion_comercial.repository.RolRepository;
import gestion_comercial.repository.UsuarioRepository;
import gestion_comercial.security.JwtService;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

@Service
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final UsuarioRepository usuarioRepository;
        private final JwtService jwtService;
        private final RolRepository rolRepository;
        private final ClienteRepository clienteRepository;
        private final PasswordEncoder passwordEncoder;

        public AuthService(
                        AuthenticationManager authenticationManager,
                        UsuarioRepository usuarioRepository,
                        JwtService jwtService,
                        RolRepository rolRepository,
                        ClienteRepository clienteRepository,
                        PasswordEncoder passwordEncoder) {

                this.authenticationManager = authenticationManager;
                this.usuarioRepository = usuarioRepository;
                this.jwtService = jwtService;
                this.rolRepository = rolRepository;
                this.clienteRepository = clienteRepository;
                this.passwordEncoder = passwordEncoder;
        }

        public LoginResponse login(LoginRequest request) {

                String email = request.email().trim();

                try {

                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        email,
                                                        request.password()));

                } catch (DisabledException exception) {

                        throw new UsuarioNoHabilitadoException(
                                        "El usuario se encuentra inactivo");

                } catch (LockedException exception) {

                        throw new UsuarioNoHabilitadoException(
                                        "El usuario se encuentra bloqueado");

                } catch (AuthenticationException exception) {

                        throw new BadCredentialsException(
                                        "Correo o contraseña incorrectos");
                }

                Usuario usuario = usuarioRepository
                                .findByEmailIgnoreCase(email)
                                .orElseThrow(() -> new BadCredentialsException(
                                                "Correo o contraseña incorrectos"));

                validarEstado(usuario);

                String token = jwtService.generateToken(usuario);

                return new LoginResponse(
                                usuario.getIdUsuario(),
                                usuario.getNombre(),
                                usuario.getApellido(),
                                usuario.getEmail(),
                                usuario.getRol().getNombre(),
                                token,
                                "Bearer",
                                "Inicio de sesión correcto");
        }

        private void validarEstado(Usuario usuario) {

                if (usuario.getEstado() == EstadoUsuario.INACTIVO) {
                        throw new UsuarioNoHabilitadoException(
                                        "El usuario se encuentra inactivo");
                }

                if (usuario.getEstado() == EstadoUsuario.BLOQUEADO) {
                        throw new UsuarioNoHabilitadoException(
                                        "El usuario se encuentra bloqueado");
                }
        }

        @Transactional
        public RegistroResponse registra(RegistroRequest request) {
                String nombre = request.nombre().trim();
                String apellido = request.apellido().trim();
                String email = request.email().trim();

                if (usuarioRepository.existsByEmailIgnoreCase(email)) {
                        throw new EmailYaRegistradoException("Ya existe un usuario registrado con este correo");
                }

                if (!request.password()
                        .equals(request.confirmacionPassword())) {
                                throw new IllegalArgumentException("la contraseña no coincide");
                        }

                Rol rolCliente = rolRepository
                        .findByNombre("CLIENTE")
                        .orElseThrow(() ->
                                new IllegalStateException("No se encontro el rol cliente")
                        );

                Usuario usuario = new Usuario();

                usuario.setNombre(nombre);
                usuario.setApellido(apellido);
                usuario.setEmail(email);
                usuario.setPasswordHash(passwordEncoder.encode(request.password()));
                usuario.setRol(rolCliente);
                usuario.setEstado(EstadoUsuario.ACTIVO);
                usuario.setFechaAlta(LocalDateTime.now());

                Usuario usuarioGuardado = usuarioRepository.save(usuario);

                Cliente cliente = new Cliente();

                cliente.setUsuario(usuarioGuardado);
                cliente.setNombre(nombre);
                cliente.setApellido(apellido);
                cliente.setCorreo(email);
                cliente.setEstado(true);

                Cliente clienteGuardado = clienteRepository.save(cliente);

                return new RegistroResponse(
                        usuarioGuardado.getIdUsuario(),
                        clienteGuardado.getIdCliente(),
                        usuarioGuardado.getNombre(),
                        usuarioGuardado.getApellido(),
                        usuarioGuardado.getEmail(),
                        usuarioGuardado.getRol().getNombre(),
                        "Se a registrado correctamente"
                );
        } 
}