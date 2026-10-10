package gestion_comercial.exception;

public class EmailYaRegistradoException extends RuntimeException{
    public EmailYaRegistradoException(String mensaje) {
        super(mensaje);
    }

}
