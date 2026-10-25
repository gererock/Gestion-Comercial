package gestion_comercial.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class DescuentoClienteException extends RuntimeException {

    private final HttpStatus status;

    public DescuentoClienteException(
            HttpStatus status,
            String message
    ) {
        super(message);
        this.status = status;
    }
}
