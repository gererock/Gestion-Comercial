package gestion_comercial.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class TestDatabaseGuardTest {
    @Test void aceptaSoloLaBaseTest() {
        assertDoesNotThrow(() -> TestDatabaseGuard.validarUrl("jdbc:postgresql://localhost:5432/gestion_comercial_test"));
        assertDoesNotThrow(() -> TestDatabaseGuard.validarNombre("gestion_comercial_test"));
    }
    @ParameterizedTest
    @ValueSource(strings={"jdbc:postgresql://localhost:5432/gestion_comercial", "jdbc:postgresql://localhost:5432/otra", "jdbc:h2:mem:test"})
    void bloqueaUrlAjenaAntesDeConectar(String url) {
        assertThrows(IllegalStateException.class, () -> TestDatabaseGuard.validarUrl(url));
    }
    @Test void compruebaTambienElNombreReal() {
        assertThrows(IllegalStateException.class, () -> TestDatabaseGuard.validarNombre("gestion_comercial"));
    }

    @Test void nombreRealIncorrectoAbortaAntesDeInicializarFlyway() throws Exception {
        var source = org.mockito.Mockito.mock(javax.sql.DataSource.class);
        var connection = org.mockito.Mockito.mock(java.sql.Connection.class);
        var statement = org.mockito.Mockito.mock(java.sql.Statement.class);
        var result = org.mockito.Mockito.mock(java.sql.ResultSet.class);
        org.mockito.Mockito.when(source.getConnection()).thenReturn(connection);
        org.mockito.Mockito.when(connection.createStatement()).thenReturn(statement);
        org.mockito.Mockito.when(statement.executeQuery("select current_database()")).thenReturn(result);
        org.mockito.Mockito.when(result.next()).thenReturn(true);
        org.mockito.Mockito.when(result.getString(1)).thenReturn("gestion_comercial");
        var flywayInicializado = new java.util.concurrent.atomic.AtomicBoolean();
        try (var context = new org.springframework.context.support.GenericApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource(
                    "test", java.util.Map.of("spring.datasource.url", "jdbc:postgresql://localhost:5432/gestion_comercial_test")));
            new TestDatabaseGuard().initialize(context);
            context.registerBean("dataSource", javax.sql.DataSource.class, () -> source);
            context.registerBean("flywaySimulado", Object.class, () -> {
                context.getBean(javax.sql.DataSource.class);
                flywayInicializado.set(true);
                return new Object();
            });
            assertThrows(org.springframework.beans.factory.BeanCreationException.class, context::refresh);
            assertFalse(flywayInicializado.get());
        }
    }
}
