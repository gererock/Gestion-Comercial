package gestion_comercial.exception;

import java.sql.SQLException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

class VentaLocalExceptionHandlerTest {
    private final VentaLocalExceptionHandler handler = new VentaLocalExceptionHandler();
    private DataIntegrityViolationException error(String state, String constraint) {
        return new DataIntegrityViolationException("DB", new ConstraintViolationException(
                "Constraint", new SQLException("DB", state), constraint));
    }
    @Test void soloClaveOperacionDuplicadaDevuelve409() {
        assertEquals(409, handler.manejarClaveDuplicada(error("23505", "ux_pedidos_clave_confirmacion"),
                new MockHttpServletRequest()).getStatusCode().value());
    }
    @Test void otraClaveUnicaNoSeOculta() {
        var error = error("23505", "pedidos_numero_pedido_key");
        assertSame(error, assertThrows(DataIntegrityViolationException.class,
                () -> handler.manejarClaveDuplicada(error, new MockHttpServletRequest())));
    }
    @Test void otroTipoDeIntegridadNoSeOculta() {
        var error = error("23503", "ux_pedidos_clave_confirmacion");
        assertSame(error, assertThrows(DataIntegrityViolationException.class,
                () -> handler.manejarClaveDuplicada(error, new MockHttpServletRequest())));
    }

    private DataIntegrityViolationException errorPostgres(String constraint) {
        var mensaje = new org.postgresql.util.ServerErrorMessage(
                "SERROR\0C23505\0Mmensaje localizado\0n" + constraint + "\0\0");
        return new DataIntegrityViolationException("DB", new ConstraintViolationException(
                "Hibernate sin nombre", new org.postgresql.util.PSQLException(mensaje), (String) null));
    }
    @Test void metadatosPostgresResuelvenNombreNuloEnHibernate() {
        assertEquals(409, handler.manejarClaveDuplicada(errorPostgres("ux_pedidos_clave_confirmacion"),
                new MockHttpServletRequest()).getStatusCode().value());
    }
    @Test void metadatosPostgresDeOtraClaveNoSeOcultan() {
        var error = errorPostgres("pedidos_numero_pedido_key");
        assertSame(error, assertThrows(DataIntegrityViolationException.class,
                () -> handler.manejarClaveDuplicada(error, new MockHttpServletRequest())));
    }
}
