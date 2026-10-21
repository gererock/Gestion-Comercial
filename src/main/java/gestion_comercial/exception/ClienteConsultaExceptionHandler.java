package gestion_comercial.exception;

import gestion_comercial.dto.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class ClienteConsultaExceptionHandler {


    @ExceptionHandler(
            ClienteNoEncontradoException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleClienteNoEncontrado(

            ClienteNoEncontradoException exception,

            HttpServletRequest request
    ) {

        ApiErrorResponse body =
                ApiErrorResponse.simple(

                        HttpStatus.NOT_FOUND.value(),

                        "Not Found",

                        exception.getMessage(),

                        request.getRequestURI()
                );


        return ResponseEntity
                .status(
                        HttpStatus.NOT_FOUND
                )
                .body(
                        body
                );
    }
}