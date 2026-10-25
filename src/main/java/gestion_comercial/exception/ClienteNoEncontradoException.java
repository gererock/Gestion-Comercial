package gestion_comercial.exception;


public class ClienteNoEncontradoException
        extends RuntimeException {


    public ClienteNoEncontradoException(
            String message
    ) {

        super(
                message
        );
    }
}