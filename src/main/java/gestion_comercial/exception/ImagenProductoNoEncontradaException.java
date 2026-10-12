package gestion_comercial.exception;


public class ImagenProductoNoEncontradaException
        extends RuntimeException {

    public ImagenProductoNoEncontradaException(
            String mensaje
    ) {

        super(mensaje);
    }
}