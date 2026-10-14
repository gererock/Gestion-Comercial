package gestion_comercial.exception;


public class MedioPagoNoEncontradoException
        extends RuntimeException {

    public MedioPagoNoEncontradoException(
            String mensaje
    ) {

        super(
                mensaje
        );
    }
}