package gestion_comercial.service.impl;
import java.math.BigDecimal;
import gestion_comercial.exception.PedidoConfirmacionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
class DescuentoClienteCalculatorTest {
    @ParameterizedTest @ValueSource(strings={"-0.01","100.01"}) void porcentajeInvalido(String valor) {
        assertThrows(PedidoConfirmacionException.class,()->PrecioPedidoCalculator.calcularDescuentoCliente(new BigDecimal("100"),new BigDecimal(valor)));
    }
    @Test void redondeaHalfUpADosDecimales() {
        assertEquals(new BigDecimal("0.01"),PrecioPedidoCalculator.calcularDescuentoCliente(new BigDecimal("0.05"),new BigDecimal("10")));
    }
}
