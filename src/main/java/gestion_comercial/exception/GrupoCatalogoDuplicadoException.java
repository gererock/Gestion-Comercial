package gestion_comercial.exception;

public class GrupoCatalogoDuplicadoException
        extends RuntimeException {

    public GrupoCatalogoDuplicadoException(
            String mensaje
    ) {
        super(mensaje);
    }
}