package gestion_comercial.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import javax.sql.DataSource;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Falla antes de Flyway/JPA si la URL o la conexión real no son de test. */
public class TestDatabaseGuard implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public static final String DATABASE = "gestion_comercial_test";

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        validarUrl(context.getEnvironment().getRequiredProperty("spring.datasource.url"));
        if (context.getEnvironment().containsProperty("spring.flyway.url")) {
            throw new IllegalStateException("US24: Flyway debe usar el DataSource de test protegido; no configurar spring.flyway.url.");
        }
        context.addBeanFactoryPostProcessor(factory -> factory.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
                if (bean instanceof DataSource source) {
                    try (Connection connection = source.getConnection();
                         Statement statement = connection.createStatement();
                         ResultSet result = statement.executeQuery("select current_database()")) {
                        if (!result.next()) throw new SQLException("No se pudo identificar la base de datos.");
                        validarNombre(result.getString(1));
                    } catch (SQLException | IllegalStateException error) {
                        throw new BeanCreationException(name,
                                "US24: conexión de test bloqueada antes de Flyway. Solo se permite " + DATABASE, error);
                    }
                }
                return bean;
            }
        }));
    }

    public static void validarUrl(String url) {
        try {
            if (!url.startsWith("jdbc:postgresql:")) throw new IllegalArgumentException();
            URI uri = URI.create(url.substring(5));
            if (!("/" + DATABASE).equals(uri.getPath())) throw new IllegalArgumentException();
        } catch (RuntimeException error) {
            throw new IllegalStateException("US24: URL de test bloqueada. Solo se permite la base " + DATABASE, error);
        }
    }

    public static void validarNombre(String nombre) {
        if (!DATABASE.equals(nombre)) {
            throw new IllegalStateException("US24: base real bloqueada: " + nombre + ". Se requiere " + DATABASE);
        }
    }
}
