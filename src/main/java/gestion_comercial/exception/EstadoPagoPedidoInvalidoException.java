package gestion_comercial.exception;


public class EstadoPagoPedidoInvalidoException
        extends RuntimeException {

    public EstadoPagoPedidoInvalidoException(
            String message
    ) {

        super(
                message
        );
    }
}