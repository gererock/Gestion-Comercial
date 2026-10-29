package gestion_comercial.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class GastoException extends RuntimeException {

    private final HttpStatus status;


    public GastoException(
            HttpStatus status,
            String message
    ) {

        super(message);

        this.status = status;
    }
}
