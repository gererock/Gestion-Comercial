package gestion_comercial.exception;


public class MedioPagoDuplicadoException
        extends RuntimeException {

    public MedioPagoDuplicadoException(
            String mensaje
    ) {

        super(
                mensaje
        );
    }
}