package gestion_comercial.exception;

import gestion_comercial.dto.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        ApiErrorResponse body = ApiErrorResponse.validation(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Existen datos inválidos",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {

        ApiErrorResponse body = ApiErrorResponse.simple(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "El cuerpo de la solicitud contiene datos inválidos",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);

    }
    @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiErrorResponse> handleBadCredentials(
                BadCredentialsException exception,
                HttpServletRequest request
        ) {

        ApiErrorResponse body = ApiErrorResponse.simple(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Correo o contraseña incorrectos",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
        }
        @ExceptionHandler(UsuarioNoHabilitadoException.class)
        public ResponseEntity<ApiErrorResponse> handleUsuarioNoHabilitado(
                UsuarioNoHabilitadoException exception,
                HttpServletRequest request
        ) {

        ApiErrorResponse body = ApiErrorResponse.simple(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(body);
        }
}
