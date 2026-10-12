package gestion_comercial.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;


@Configuration
public class WebConfig
        implements WebMvcConfigurer {

    private final String
            directorioProductos;


    public WebConfig(
            @Value("${app.upload.productos-dir}")
            String directorioProductos
    ) {

        this.directorioProductos =
                directorioProductos;
    }


    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {

        Path ruta =
                Paths.get(
                                directorioProductos
                        )
                        .toAbsolutePath()
                        .normalize();


        String ubicacion =
                ruta
                        .toUri()
                        .toString();


        if (
                !ubicacion.endsWith("/")
        ) {

            ubicacion =
                    ubicacion + "/";
        }


        registry
                .addResourceHandler(
                        "/images/productos/**"
                )
                .addResourceLocations(
                        ubicacion
                );
    }
}