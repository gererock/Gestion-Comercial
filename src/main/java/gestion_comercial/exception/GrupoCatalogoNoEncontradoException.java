package gestion_comercial.exception;

public class GrupoCatalogoNoEncontradoException
        extends RuntimeException {

    public GrupoCatalogoNoEncontradoException(
            String mensaje
    ) {
        super(mensaje);
    }
}