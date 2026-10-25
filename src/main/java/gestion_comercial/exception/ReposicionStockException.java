package gestion_comercial.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class ReposicionStockException extends RuntimeException {

    private final HttpStatus status;

    public ReposicionStockException(
            HttpStatus status,
            String message
    ) {
        super(message);
        this.status = status;
    }
}
