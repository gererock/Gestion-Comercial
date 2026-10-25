package gestion_comercial.service.impl;

import gestion_comercial.dto.request.ReposicionStockRequest;
import gestion_comercial.dto.response.ReposicionStockResponse;
import gestion_comercial.entity.MovimientoStock;
import gestion_comercial.entity.Producto;
import gestion_comercial.entity.Usuario;
import gestion_comercial.exception.ReposicionStockException;
import gestion_comercial.repository.MovimientoStockRepository;
import gestion_comercial.repository.ProductoRepository;
import gestion_comercial.repository.UsuarioRepository;
import gestion_comercial.service.interfaces.IReposicionStockService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReposicionStockService implements IReposicionStockService {

    static final int CANTIDAD_MAXIMA = 1_000_000;

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String TIPO_ENTRADA = "ENTRADA";
    private static final String ORIGEN_REPOSICION = "REPOSICION_STOCK";

    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public ReposicionStockResponse registrar(
            ReposicionStockRequest request,
            String emailUsuario
    ) {
        int idProducto = convertirIdProducto(request.idProducto());
        int cantidad = convertirCantidad(request.cantidad());
        Usuario usuario = obtenerAdministrador(emailUsuario);

        Producto producto = productoRepository
                .buscarParaReponerStock(idProducto)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "El producto indicado no existe"
                ));

        MovimientoStock repetido = movimientoStockRepository
                .findByClaveOperacion(request.claveOperacion())
                .orElse(null);

        if (repetido != null) {
            return resolverOperacionRepetida(
                    repetido,
                    producto,
                    usuario,
                    cantidad
            );
        }

        int stockAnterior = producto.getStockActual() == null
                ? 0
                : producto.getStockActual();

        if (stockAnterior < 0) {
            throw error(
                    HttpStatus.CONFLICT,
                    "El producto posee un stock actual inválido"
            );
        }

        long calculado = (long) stockAnterior + cantidad;

        if (calculado > Integer.MAX_VALUE) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La reposición supera el stock máximo permitido"
            );
        }

        int stockPosterior = (int) calculado;
        LocalDateTime fecha = LocalDateTime.now();

        producto.setStockActual(stockPosterior);
        productoRepository.save(producto);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProducto(producto);
        movimiento.setUsuario(usuario);
        movimiento.setTipoMovimiento(TIPO_ENTRADA);
        movimiento.setOrigen(ORIGEN_REPOSICION);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockPosterior(stockPosterior);
        movimiento.setFecha(fecha);
        movimiento.setClaveOperacion(request.claveOperacion());

        MovimientoStock guardado = movimientoStockRepository
                .saveAndFlush(movimiento);

        return convertirRespuesta(
                guardado,
                "Reposición registrada correctamente"
        );
    }

    private int convertirIdProducto(String valor) {
        try {
            int idProducto = Integer.parseInt(valor);

            if (idProducto <= 0) {
                throw new NumberFormatException();
            }

            return idProducto;
        } catch (NumberFormatException exception) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El producto seleccionado no es válido"
            );
        }
    }

    private int convertirCantidad(String valor) {
        try {
            int cantidad = Integer.parseInt(valor);

            if (cantidad <= 0) {
                throw new NumberFormatException();
            }

            if (cantidad > CANTIDAD_MAXIMA) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "La cantidad máxima permitida por reposición es 1000000"
                );
            }

            return cantidad;
        } catch (NumberFormatException exception) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad debe ser un número entero mayor a cero"
            );
        }
    }

    private Usuario obtenerAdministrador(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw error(
                    HttpStatus.UNAUTHORIZED,
                    "No se pudo identificar al usuario que realiza la reposición"
            );
        }

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(emailUsuario)
                .orElseThrow(() -> error(
                        HttpStatus.UNAUTHORIZED,
                        "No se pudo identificar al usuario que realiza la reposición"
                ));

        if (usuario.getRol() == null
                || !ROL_ADMINISTRADOR.equals(usuario.getRol().getNombre())) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "Solo un administrador puede registrar reposiciones de stock"
            );
        }

        return usuario;
    }

    private ReposicionStockResponse resolverOperacionRepetida(
            MovimientoStock movimiento,
            Producto producto,
            Usuario usuario,
            int cantidad
    ) {
        boolean coincide = movimiento.getProducto().getIdProducto()
                .equals(producto.getIdProducto())
                && movimiento.getUsuario().getIdUsuario()
                .equals(usuario.getIdUsuario())
                && movimiento.getCantidad().equals(cantidad)
                && TIPO_ENTRADA.equals(movimiento.getTipoMovimiento())
                && ORIGEN_REPOSICION.equals(movimiento.getOrigen());

        if (!coincide) {
            throw error(
                    HttpStatus.CONFLICT,
                    "La identificación de la operación ya fue utilizada"
            );
        }

        return convertirRespuesta(
                movimiento,
                "La reposición ya había sido registrada"
        );
    }

    private ReposicionStockResponse convertirRespuesta(
            MovimientoStock movimiento,
            String mensaje
    ) {
        return new ReposicionStockResponse(
                movimiento.getIdMovimientoStock(),
                movimiento.getProducto().getIdProducto(),
                movimiento.getProducto().getNombre(),
                movimiento.getUsuario().getIdUsuario(),
                movimiento.getTipoMovimiento(),
                movimiento.getOrigen(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockPosterior(),
                movimiento.getFecha(),
                mensaje
        );
    }

    private ReposicionStockException error(
            HttpStatus status,
            String mensaje
    ) {
        return new ReposicionStockException(status, mensaje);
    }
}
