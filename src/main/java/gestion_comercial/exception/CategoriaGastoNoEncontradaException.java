package gestion_comercial.exception;

public class CategoriaGastoNoEncontradaException
        extends RuntimeException {

    public CategoriaGastoNoEncontradaException(
            String mensaje
    ) {

        super(mensaje);
    }
}