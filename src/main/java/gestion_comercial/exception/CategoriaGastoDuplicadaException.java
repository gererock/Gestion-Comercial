package gestion_comercial.exception;

public class CategoriaGastoDuplicadaException
        extends RuntimeException {

    public CategoriaGastoDuplicadaException(
            String mensaje
    ) {

        super(mensaje);
    }
}