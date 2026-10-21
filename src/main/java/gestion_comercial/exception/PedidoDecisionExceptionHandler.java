package gestion_comercial.exception;

import gestion_comercial.dto.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PedidoDecisionExceptionHandler {

    @ExceptionHandler(PedidoDecisionInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> handleDecisionInvalida(
            PedidoDecisionInvalidaException exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse body = ApiErrorResponse.simple(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }
}
