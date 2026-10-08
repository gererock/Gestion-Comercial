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

                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth

                                                // Archivos públicos del frontend
                                                .requestMatchers(
                                                                "/",
                                                                "/index.html",
                                                                "/categorias.html",
                                                                "/marcas.html",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/data/**",
                                                                "/img/**",
                                                                "/images/**",
                                                                "/login/**",
                                                                "/admin/**",
                                                                "/vendedor/**",
                                                                "/cliente/**",
                                                                "/catalogo/**",
                                                                "/grupos-catalogo.html")
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

                                                // =========================
                                                // CATEGORÍAS
                                                // =========================

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/categorias",
                                                                "/api/categorias/**")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "VENDEDOR")

                                                .requestMatchers(
                                                                "/api/categorias",
                                                                "/api/categorias/**")
                                                .hasRole("ADMINISTRADOR")

                                                // =========================
                                                // GRUPOS DE CATÁLOGO
                                                // =========================

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/grupos-catalogo",
                                                                "/api/grupos-catalogo/**")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "VENDEDOR")

                                                .requestMatchers(
                                                                "/api/grupos-catalogo",
                                                                "/api/grupos-catalogo/**")
                                                .hasRole("ADMINISTRADOR")

                                                // =========================
                                                // MARCAS
                                                // =========================

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/marcas",
                                                                "/api/marcas/**")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "VENDEDOR")

                                                .requestMatchers(
                                                                "/api/marcas",
                                                                "/api/marcas/**")
                                                .hasRole("ADMINISTRADOR")

                                                // =========================
                                                // PRODUCTOS - US27
                                                // =========================

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/productos")
                                                .hasRole("ADMINISTRADOR")

                                                // =========================
                                                // ROLES
                                                // =========================

                                                .requestMatchers("/api/admin/**")
                                                .hasRole("ADMINISTRADOR")

                                                .requestMatchers("/api/vendedor/**")
                                                .hasAnyRole(
                                                                "VENDEDOR",
                                                                "ADMINISTRADOR")

                                                .requestMatchers("/api/cliente/**")
                                                .hasRole("CLIENTE")

                                                // SIEMPRE ÚLTIMO
                                                .anyRequest()
                                                .authenticated())

                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(
                                                                authenticationEntryPoint)
                                                .accessDeniedHandler(
                                                                accessDeniedHandler))

                                .formLogin(form -> form.disable())

                                .httpBasic(basic -> basic.disable())

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

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

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(
                                Arrays.stream(
                                                allowedOrigins.split(","))
                                                .map(String::trim)
                                                .toList());

                configuration.setAllowedMethods(
                                List.of(
                                                "GET",
                                                "POST",
                                                "PUT",
                                                "PATCH",
                                                "DELETE",
                                                "OPTIONS"));

                configuration.setAllowedHeaders(
                                List.of(
                                                "Authorization",
                                                "Content-Type",
                                                "Accept"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration(
                                "/**",
                                configuration);

                return source;
        }
}