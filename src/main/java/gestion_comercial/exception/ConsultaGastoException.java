package gestion_comercial.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;


@Getter
public class ConsultaGastoException
        extends RuntimeException {

    private final HttpStatus status;


    public ConsultaGastoException(
            HttpStatus status,
            String message
    ) {

        super(message);

        this.status = status;
    }
}