package gestion_comercial.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class ConsultaStockBajoException extends RuntimeException {

    private final HttpStatus status;

    public ConsultaStockBajoException(
            HttpStatus status,
            String message
    ) {
        super(message);
        this.status = status;
    }
}
