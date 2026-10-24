package gestion_comercial.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class ClienteUsuarioAsociacionException
        extends RuntimeException {

    private final HttpStatus status;

    public ClienteUsuarioAsociacionException(
            HttpStatus status,
            String message
    ) {
        super(message);
        this.status = status;
    }
}
