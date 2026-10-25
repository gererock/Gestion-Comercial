package gestion_comercial.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class ConsultaMovimientoStockException extends RuntimeException {

    private final HttpStatus status;

    public ConsultaMovimientoStockException(
            HttpStatus status,
            String message
    ) {
        super(message);
        this.status = status;
    }
}
