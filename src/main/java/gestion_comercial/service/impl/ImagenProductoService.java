package gestion_comercial.service.impl;

import gestion_comercial.dto.request.ImagenProductoOrdenRequest;
import gestion_comercial.dto.response.ImagenProductoResponse;

import gestion_comercial.entity.ImagenProducto;
import gestion_comercial.entity.Producto;

import gestion_comercial.exception.ImagenProductoAlmacenamientoException;
import gestion_comercial.exception.ImagenProductoNoEncontradaException;
import gestion_comercial.exception.ProductoNoEncontradoException;

import gestion_comercial.mapper.ImagenProductoMapper;

import gestion_comercial.repository.ImagenProductoRepository;
import gestion_comercial.repository.ProductoRepository;

import gestion_comercial.service.interfaces.IImagenProductoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


@Service
public class ImagenProductoService
        implements IImagenProductoService {

    private static final long TAMANIO_MAXIMO =
            5L * 1024L * 1024L;


    private static final Set<String>
            EXTENSIONES_PERMITIDAS =
            Set.of(
                    "jpg",
                    "jpeg",
                    "png"
            );


    private static final Set<String>
            TIPOS_PERMITIDOS =
            Set.of(
                    "image/jpeg",
                    "image/jpg",
                    "image/png"
            );


    private final ImagenProductoRepository
            imagenProductoRepository;

    private final ProductoRepository
            productoRepository;

    private final Path
            directorioProductos;


    public ImagenProductoService(
            ImagenProductoRepository imagenProductoRepository,
            ProductoRepository productoRepository,

            @Value("${app.upload.productos-dir}")
            String directorioProductos
    ) {

        this.imagenProductoRepository =
                imagenProductoRepository;

        this.productoRepository =
                productoRepository;

        this.directorioProductos =
                Paths.get(
                                directorioProductos
                        )
                        .toAbsolutePath()
                        .normalize();
    }


    // =========================================
    // LISTAR
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public List<ImagenProductoResponse> listar(
            Integer idProducto
    ) {

        buscarProducto(
                idProducto
        );


        return imagenProductoRepository
                .findByProducto_IdProductoOrderByOrdenAscIdImagenAsc(
                        idProducto
                )
                .stream()
                .map(
                        ImagenProductoMapper::toResponse
                )
                .toList();
    }


    // =========================================
    // AGREGAR
    // =========================================

    @Override
    @Transactional
    public ImagenProductoResponse agregar(
            Integer idProducto,
            MultipartFile archivo,
            Integer orden,
            Boolean principal
    ) {

        Producto producto =
                buscarProducto(
                        idProducto
                );


        validarArchivo(
                archivo
        );


        if (
                orden != null
                        &&
                orden < 0
        ) {

            throw new IllegalArgumentException(
                    "El orden no puede ser negativo"
            );
        }


        Integer ordenFinal =
                orden != null
                        ? orden
                        : obtenerSiguienteOrden(
                                idProducto
                        );


        String urlImagen =
                guardarArchivo(
                        archivo
                );


        try {

            if (
                    Boolean.TRUE.equals(
                            principal
                    )
            ) {

                quitarPrincipalActual(
                        idProducto,
                        null
                );
            }


            ImagenProducto imagen =
                    new ImagenProducto();


            imagen.setProducto(
                    producto
            );


            imagen.setUrlImagen(
                    urlImagen
            );


            imagen.setOrden(
                    ordenFinal
            );


            imagen.setPrincipal(
                    Boolean.TRUE.equals(
                            principal
                    )
            );


            ImagenProducto imagenGuardada =
                    imagenProductoRepository
                            .saveAndFlush(
                                    imagen
                            );


            return ImagenProductoMapper
                    .toResponse(
                            imagenGuardada
                    );


        } catch (RuntimeException exception) {

            eliminarArchivoSilenciosamente(
                    urlImagen
            );

            throw exception;
        }
    }


    // =========================================
    // DEFINIR PRINCIPAL
    // =========================================

    @Override
    @Transactional
    public ImagenProductoResponse definirPrincipal(
            Integer idProducto,
            Integer idImagen
    ) {

        buscarProducto(
                idProducto
        );


        ImagenProducto imagen =
                buscarImagenDelProducto(
                        idProducto,
                        idImagen
                );


        if (
                Boolean.TRUE.equals(
                        imagen.getPrincipal()
                )
        ) {

            return ImagenProductoMapper
                    .toResponse(
                            imagen
                    );
        }


        quitarPrincipalActual(
                idProducto,
                idImagen
        );


        imagen.setPrincipal(
                true
        );


        ImagenProducto imagenActualizada =
                imagenProductoRepository
                        .saveAndFlush(
                                imagen
                        );


        return ImagenProductoMapper
                .toResponse(
                        imagenActualizada
                );
    }


    // =========================================
    // CAMBIAR ORDEN
    // =========================================

    @Override
    @Transactional
    public ImagenProductoResponse cambiarOrden(
            Integer idProducto,
            Integer idImagen,
            ImagenProductoOrdenRequest request
    ) {

        buscarProducto(
                idProducto
        );


        ImagenProducto imagen =
                buscarImagenDelProducto(
                        idProducto,
                        idImagen
                );


        imagen.setOrden(
                request.orden()
        );


        ImagenProducto imagenActualizada =
                imagenProductoRepository
                        .saveAndFlush(
                                imagen
                        );


        return ImagenProductoMapper
                .toResponse(
                        imagenActualizada
                );
    }


    // =========================================
    // ELIMINAR
    // =========================================

    @Override
    @Transactional
    public void eliminar(
            Integer idProducto,
            Integer idImagen
    ) {

        buscarProducto(
                idProducto
        );


        ImagenProducto imagen =
                buscarImagenDelProducto(
                        idProducto,
                        idImagen
                );


        String urlImagen =
                imagen.getUrlImagen();


        imagenProductoRepository
                .delete(
                        imagen
                );


        imagenProductoRepository
                .flush();


        eliminarArchivo(
                urlImagen
        );
    }


    // =========================================
    // PRODUCTO
    // =========================================

    private Producto buscarProducto(
            Integer idProducto
    ) {

        return productoRepository
                .findById(
                        idProducto
                )
                .orElseThrow(
                        () ->
                                new ProductoNoEncontradoException(
                                        "No existe un producto con este ID"
                                )
                );
    }


    // =========================================
    // IMAGEN
    // =========================================

    private ImagenProducto buscarImagenDelProducto(
            Integer idProducto,
            Integer idImagen
    ) {

        return imagenProductoRepository
                .findByIdImagenAndProducto_IdProducto(
                        idImagen,
                        idProducto
                )
                .orElseThrow(
                        () ->
                                new ImagenProductoNoEncontradaException(
                                        "No se encontró la imagen para el producto indicado"
                                )
                );
    }


    // =========================================
    // PRINCIPAL
    // =========================================

    private void quitarPrincipalActual(
            Integer idProducto,
            Integer idImagenExcluir
    ) {

        imagenProductoRepository
                .findFirstByProducto_IdProductoAndPrincipalTrue(
                        idProducto
                )
                .ifPresent(
                        imagenPrincipal -> {

                            if (
                                    idImagenExcluir != null
                                            &&
                                    imagenPrincipal
                                            .getIdImagen()
                                            .equals(
                                                    idImagenExcluir
                                            )
                            ) {

                                return;
                            }


                            imagenPrincipal.setPrincipal(
                                    false
                            );


                            /*
                             * Se fuerza el UPDATE antes de marcar
                             * otra imagen como principal para
                             * respetar el índice único de PostgreSQL.
                             */
                            imagenProductoRepository
                                    .saveAndFlush(
                                            imagenPrincipal
                                    );
                        }
                );
    }


    // =========================================
    // ORDEN
    // =========================================

    private Integer obtenerSiguienteOrden(
            Integer idProducto
    ) {

        Integer ordenMaximo =
                imagenProductoRepository
                        .obtenerOrdenMaximo(
                                idProducto
                        );


        if (ordenMaximo == null) {

            return 1;
        }


        return ordenMaximo + 1;
    }


    // =========================================
    // VALIDACIÓN DE ARCHIVO
    // =========================================

    private void validarArchivo(
            MultipartFile archivo
    ) {

        if (
                archivo == null
                        ||
                archivo.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una imagen"
            );
        }


        if (
                archivo.getSize()
                        >
                TAMANIO_MAXIMO
        ) {

            throw new IllegalArgumentException(
                    "La imagen no puede superar los 5 MB"
            );
        }


        String nombreOriginal =
                archivo.getOriginalFilename();


        if (
                nombreOriginal == null
                        ||
                nombreOriginal.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El archivo seleccionado no tiene un nombre válido"
            );
        }


        String extension =
                obtenerExtension(
                        nombreOriginal
                );


        if (
                !EXTENSIONES_PERMITIDAS
                        .contains(
                                extension
                        )
        ) {

            throw new IllegalArgumentException(
                    "Solo se permiten imágenes JPG, JPEG o PNG"
            );
        }


        String tipoContenido =
                archivo.getContentType();


        if (
                tipoContenido == null
                        ||
                !TIPOS_PERMITIDOS
                        .contains(
                                tipoContenido
                                        .toLowerCase(
                                                Locale.ROOT
                                        )
                        )
        ) {

            throw new IllegalArgumentException(
                    "El archivo seleccionado no tiene un formato de imagen permitido"
            );
        }


        validarContenidoImagen(
                archivo
        );
    }


    private void validarContenidoImagen(
            MultipartFile archivo
    ) {

        try (
                InputStream inputStream =
                        archivo.getInputStream()
        ) {

            if (
                    ImageIO.read(
                            inputStream
                    )
                            ==
                    null
            ) {

                throw new IllegalArgumentException(
                        "La imagen está dañada o no puede leerse"
                );
            }


        } catch (IOException exception) {

            throw new IllegalArgumentException(
                    "La imagen está dañada o no puede leerse"
            );
        }
    }


    private String obtenerExtension(
            String nombreArchivo
    ) {

        int posicionPunto =
                nombreArchivo
                        .lastIndexOf(
                                '.'
                        );


        if (
                posicionPunto < 0
                        ||
                posicionPunto
                        ==
                nombreArchivo.length() - 1
        ) {

            return "";
        }


        return nombreArchivo
                .substring(
                        posicionPunto + 1
                )
                .toLowerCase(
                        Locale.ROOT
                );
    }


    // =========================================
    // ALMACENAMIENTO
    // =========================================

    private String guardarArchivo(
            MultipartFile archivo
    ) {

        String extension =
                obtenerExtension(
                        archivo.getOriginalFilename()
                );


        String nombreGenerado =
                UUID.randomUUID()
                        + "."
                        + extension;


        Path destino =
                directorioProductos
                        .resolve(
                                nombreGenerado
                        )
                        .normalize();


        if (
                !destino.startsWith(
                        directorioProductos
                )
        ) {

            throw new ImagenProductoAlmacenamientoException(
                    "No se pudo determinar una ubicación segura para la imagen"
            );
        }


        try {

            Files.createDirectories(
                    directorioProductos
            );


            try (
                    InputStream inputStream =
                            archivo.getInputStream()
            ) {

                Files.copy(
                        inputStream,
                        destino
                );
            }


        } catch (IOException exception) {

            throw new ImagenProductoAlmacenamientoException(
                    "No se pudo guardar la imagen",
                    exception
            );
        }


        return "/images/productos/"
                + nombreGenerado;
    }


    private void eliminarArchivo(
            String urlImagen
    ) {

        Path archivo =
                obtenerRutaDesdeUrl(
                        urlImagen
                );


        try {

            Files.deleteIfExists(
                    archivo
            );


        } catch (IOException exception) {

            throw new ImagenProductoAlmacenamientoException(
                    "No se pudo eliminar el archivo de imagen",
                    exception
            );
        }
    }


    private void eliminarArchivoSilenciosamente(
            String urlImagen
    ) {

        try {

            Files.deleteIfExists(
                    obtenerRutaDesdeUrl(
                            urlImagen
                    )
            );

        } catch (IOException ignored) {
        }
    }


    private Path obtenerRutaDesdeUrl(
            String urlImagen
    ) {

        String nombreArchivo =
                Paths.get(
                                urlImagen
                        )
                        .getFileName()
                        .toString();


        return directorioProductos
                .resolve(
                        nombreArchivo
                )
                .normalize();
    }
}