package gestion_comercial.config;

import gestion_comercial.exception.RestAccessDeniedHandler;
import gestion_comercial.exception.RestAuthenticationEntryPoint;
import gestion_comercial.security.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Archivos públicos del frontend
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/categorias.html",
                                "/css/**",
                                "/js/**",
                                "/data/**",
                                "/img/**",
                                "/images/**",
                                "/login/**",
                                "/admin/**",
                                "/vendedor/**",
                                "/cliente/**",
                                "/catalogo/**"
                        )
                        .permitAll()

                        // Endpoint de errores
                        .requestMatchers("/error")
                        .permitAll()

                        // Login / autenticación
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // Endpoints públicos
                        .requestMatchers("/api/public/**")
                        .permitAll()

                        // ADMINISTRADOR y VENDEDOR pueden consultar categorías
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categorias",
                                "/api/categorias/**"
                        )
                        .hasAnyRole(
                                "ADMINISTRADOR",
                                "VENDEDOR"
                        )

                        // Solo ADMINISTRADOR puede modificar categorías
                        .requestMatchers(
                                "/api/categorias",
                                "/api/categorias/**"
                        )
                        .hasRole("ADMINISTRADOR")

                        // Endpoints solo para administrador
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMINISTRADOR")

                        // Vendedor y administrador
                        .requestMatchers("/api/vendedor/**")
                        .hasAnyRole(
                                "VENDEDOR",
                                "ADMINISTRADOR"
                        )

                        // Solo cliente
                        .requestMatchers("/api/cliente/**")
                        .hasRole("CLIENTE")

                        // Todo lo demás requiere autenticación
                        .anyRequest()
                        .authenticated()
                )

                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {

        return authenticationConfiguration
                .getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                Arrays.stream(
                                allowedOrigins.split(",")
                        )
                        .map(String::trim)
                        .toList()
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}