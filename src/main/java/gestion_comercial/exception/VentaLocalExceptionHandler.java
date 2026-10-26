package gestion_comercial.exception;

import gestion_comercial.controller.VentaLocalController;
import gestion_comercial.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** El interceptor transaccional ya hizo rollback antes de llegar al handler MVC. */
@RestControllerAdvice(assignableTypes = VentaLocalController.class)
public class VentaLocalExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> manejarClaveDuplicada(
            DataIntegrityViolationException error, HttpServletRequest request) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (esClaveDuplicada(causa)) {
                return ResponseEntity.status(409).body(ApiErrorResponse.simple(409, "Conflict",
                        "La clave de operación ya fue utilizada.", request.getRequestURI()));
            }
        }
        // No convertir errores de FK, CHECK, NOT NULL u otras claves únicas en idempotencia.
        throw error;
    }

    private boolean esClaveDuplicada(Throwable causa) {
        if (causa instanceof ConstraintViolationException constraint) {
            return "23505".equals(constraint.getSQLState())
                    && "ux_pedidos_clave_confirmacion".equals(constraint.getConstraintName());
        }
        if (causa instanceof PSQLException postgres) {
            // Metadatos del protocolo PostgreSQL; no analizar mensajes localizados.
            return "23505".equals(postgres.getSQLState())
                    && postgres.getServerErrorMessage() != null
                    && "ux_pedidos_clave_confirmacion".equals(postgres.getServerErrorMessage().getConstraint());
        }
        return false;
    }
}
