package gestion_comercial.service;

import gestion_comercial.dto.request.LoginRequest;
import gestion_comercial.dto.response.LoginResponse;
import gestion_comercial.entity.EstadoUsuario;
import gestion_comercial.entity.Usuario;
import gestion_comercial.exception.UsuarioNoHabilitadoException;
import gestion_comercial.repository.UsuarioRepository;
import gestion_comercial.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        String email = request.email().trim();

        try {

                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                request.password()
                        )
                );

                } catch (DisabledException exception) {

                throw new UsuarioNoHabilitadoException(
                        "El usuario se encuentra inactivo"
                );

                } catch (LockedException exception) {

                throw new UsuarioNoHabilitadoException(
                        "El usuario se encuentra bloqueado"
                );

                } catch (AuthenticationException exception) {

                throw new BadCredentialsException(
                        "Correo o contraseña incorrectos"
                );
                }

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Correo o contraseña incorrectos"
                        )
                );

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
                "Inicio de sesión correcto"
        );
    }

    private void validarEstado(Usuario usuario) {

        if (usuario.getEstado() == EstadoUsuario.INACTIVO) {
            throw new UsuarioNoHabilitadoException(
                    "El usuario se encuentra inactivo"
            );
        }

        if (usuario.getEstado() == EstadoUsuario.BLOQUEADO) {
            throw new UsuarioNoHabilitadoException(
                    "El usuario se encuentra bloqueado"
            );
        }
    }
}