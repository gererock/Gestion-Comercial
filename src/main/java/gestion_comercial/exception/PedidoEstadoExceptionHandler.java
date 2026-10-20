package gestion_comercial.exception;

import gestion_comercial.dto.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PedidoEstadoExceptionHandler {

    @ExceptionHandler(EstadoPedidoInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleEstadoInvalido(
            EstadoPedidoInvalidoException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.simple(
                        HttpStatus.CONFLICT.value(),
                        "Conflict",
                        exception.getMessage(),
                        request.getRequestURI()
                ));
    }
}
