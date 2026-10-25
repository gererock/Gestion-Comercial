package gestion_comercial.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class AjusteManualStockException extends RuntimeException {

    private final HttpStatus status;

    public AjusteManualStockException(
            HttpStatus status,
            String message
    ) {
        super(message);
        this.status = status;
    }
}
