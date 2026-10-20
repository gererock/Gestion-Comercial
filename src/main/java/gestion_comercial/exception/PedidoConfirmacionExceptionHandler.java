
package gestion_comercial.exception;

import gestion_comercial.dto.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PedidoConfirmacionExceptionHandler {

    @ExceptionHandler(PedidoConfirmacionException.class)
    public ResponseEntity<ApiErrorResponse> manejar(
            PedidoConfirmacionException error,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.simple(
                        409,
                        "Conflict",
                        error.getMessage(),
                        request.getRequestURI()
                ));
    }
}
