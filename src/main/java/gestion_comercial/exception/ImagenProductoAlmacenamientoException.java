package gestion_comercial.exception;


public class ImagenProductoAlmacenamientoException
        extends RuntimeException {

    public ImagenProductoAlmacenamientoException(
            String mensaje
    ) {

        super(mensaje);
    }


    public ImagenProductoAlmacenamientoException(
            String mensaje,
            Throwable causa
    ) {

        super(
                mensaje,
                causa
        );
    }
}