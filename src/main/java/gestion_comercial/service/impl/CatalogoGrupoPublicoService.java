package gestion_comercial.service.impl;

import gestion_comercial.dto.response.CatalogoOpcionGrupoResponse;

import gestion_comercial.entity.EstadoProducto;
import gestion_comercial.entity.ImagenProducto;
import gestion_comercial.entity.Producto;

import gestion_comercial.repository.CatalogoGrupoProductoRepository;
import gestion_comercial.repository.ImagenProductoRepository;

import gestion_comercial.service.interfaces.ICatalogoGrupoPublicoService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class CatalogoGrupoPublicoService
        implements ICatalogoGrupoPublicoService {

    private final CatalogoGrupoProductoRepository
            catalogoGrupoProductoRepository;

    private final ImagenProductoRepository
            imagenProductoRepository;


    public CatalogoGrupoPublicoService(
            CatalogoGrupoProductoRepository catalogoGrupoProductoRepository,
            ImagenProductoRepository imagenProductoRepository
    ) {

        this.catalogoGrupoProductoRepository =
                catalogoGrupoProductoRepository;

        this.imagenProductoRepository =
                imagenProductoRepository;
    }


    // =========================================
    // OPCIONES PÚBLICAS DE GRUPO - US05
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public List<CatalogoOpcionGrupoResponse> obtenerOpciones(
            Integer idGrupoCatalogo
    ) {

        if (
                idGrupoCatalogo == null
                ||
                idGrupoCatalogo <= 0
        ) {

            return List.of();
        }


        List<Producto> productos =
                catalogoGrupoProductoRepository
                        .buscarOpcionesPublicas(

                                idGrupoCatalogo,

                                EstadoProducto.ACTIVO
                        );


        if (
                productos.isEmpty()
        ) {

            return List.of();
        }


        Map<Integer, String> imagenesPrincipales =
                obtenerImagenesPrincipales(
                        productos
                );


        return productos
                .stream()
                .map(
                        producto ->
                                new CatalogoOpcionGrupoResponse(

                                        producto.getIdProducto(),

                                        producto.getNombre(),

                                        imagenesPrincipales.get(
                                                producto.getIdProducto()
                                        ),

                                        producto.getPrecioVenta(),

                                        tieneStock(
                                                producto
                                        )
                                )
                )
                .toList();
    }


    private boolean tieneStock(
            Producto producto
    ) {

        return producto.getStockActual() != null
                &&
                producto.getStockActual() > 0;
    }


    private Map<Integer, String>
    obtenerImagenesPrincipales(
            List<Producto> productos
    ) {

        List<Integer> idsProductos =
                productos
                        .stream()
                        .map(
                                Producto::getIdProducto
                        )
                        .toList();


        List<ImagenProducto> imagenes =
                imagenProductoRepository
                        .buscarPrincipalesPorProductos(
                                idsProductos
                        );


        Map<Integer, String> resultado =
                new LinkedHashMap<>();


        for (
                ImagenProducto imagen
                : imagenes
        ) {

            resultado.put(
                    imagen
                            .getProducto()
                            .getIdProducto(),

                    imagen.getUrlImagen()
            );
        }


        return resultado;
    }
}