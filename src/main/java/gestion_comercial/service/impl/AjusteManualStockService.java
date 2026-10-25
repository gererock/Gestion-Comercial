package gestion_comercial.service.impl;

import gestion_comercial.dto.request.AjusteManualStockRequest;
import gestion_comercial.dto.response.AjusteManualStockResponse;
import gestion_comercial.entity.MovimientoStock;
import gestion_comercial.entity.Producto;
import gestion_comercial.entity.Usuario;
import gestion_comercial.exception.AjusteManualStockException;
import gestion_comercial.repository.MovimientoStockRepository;
import gestion_comercial.repository.ProductoRepository;
import gestion_comercial.repository.UsuarioRepository;
import gestion_comercial.service.interfaces.IAjusteManualStockService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AjusteManualStockService implements IAjusteManualStockService {

    static final int CANTIDAD_MAXIMA = 1_000_000;

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String AJUSTE_POSITIVO = "POSITIVO";
    private static final String AJUSTE_NEGATIVO = "NEGATIVO";
    private static final String TIPO_AJUSTE_POSITIVO = "AJUSTE_POSITIVO";
    private static final String TIPO_AJUSTE_NEGATIVO = "AJUSTE_NEGATIVO";
    private static final String ORIGEN_AJUSTE_MANUAL = "AJUSTE_MANUAL";

    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public AjusteManualStockResponse ajustar(
            AjusteManualStockRequest request,
            String emailUsuario
    ) {
        int idProducto = convertirIdProducto(request.idProducto());
        int cantidad = convertirCantidad(request.cantidad());
        String tipoMovimiento = convertirTipoMovimiento(request.tipoAjuste());
        Usuario usuario = obtenerAdministrador(emailUsuario);

        Producto producto = productoRepository
                .buscarParaAjustarStock(idProducto)
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
                    tipoMovimiento,
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

        int stockPosterior = calcularStockPosterior(
                stockAnterior,
                cantidad,
                tipoMovimiento
        );
        LocalDateTime fecha = LocalDateTime.now();

        producto.setStockActual(stockPosterior);
        productoRepository.save(producto);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProducto(producto);
        movimiento.setUsuario(usuario);
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setOrigen(ORIGEN_AJUSTE_MANUAL);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockPosterior(stockPosterior);
        movimiento.setFecha(fecha);
        movimiento.setClaveOperacion(request.claveOperacion());

        MovimientoStock guardado = movimientoStockRepository
                .saveAndFlush(movimiento);

        return convertirRespuesta(
                guardado,
                "Ajuste manual registrado correctamente"
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
                        "La cantidad máxima permitida por ajuste es 1000000"
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

    private String convertirTipoMovimiento(String tipoAjuste) {
        if (AJUSTE_POSITIVO.equals(tipoAjuste)) {
            return TIPO_AJUSTE_POSITIVO;
        }

        if (AJUSTE_NEGATIVO.equals(tipoAjuste)) {
            return TIPO_AJUSTE_NEGATIVO;
        }

        throw error(
                HttpStatus.BAD_REQUEST,
                "El tipo de ajuste seleccionado no es válido"
        );
    }

    private Usuario obtenerAdministrador(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw error(
                    HttpStatus.UNAUTHORIZED,
                    "No se pudo identificar al usuario que realiza el ajuste"
            );
        }

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(emailUsuario)
                .orElseThrow(() -> error(
                        HttpStatus.UNAUTHORIZED,
                        "No se pudo identificar al usuario que realiza el ajuste"
                ));

        if (usuario.getRol() == null
                || !ROL_ADMINISTRADOR.equals(usuario.getRol().getNombre())) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "Solo un administrador puede realizar ajustes manuales de stock"
            );
        }

        return usuario;
    }

    private int calcularStockPosterior(
            int stockAnterior,
            int cantidad,
            String tipoMovimiento
    ) {
        if (TIPO_AJUSTE_NEGATIVO.equals(tipoMovimiento)) {
            if (cantidad > stockAnterior) {
                throw error(
                        HttpStatus.CONFLICT,
                        "El ajuste negativo no puede dejar el stock por debajo de cero"
                );
            }

            return stockAnterior - cantidad;
        }

        long calculado = (long) stockAnterior + cantidad;

        if (calculado > Integer.MAX_VALUE) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El ajuste supera el stock máximo permitido"
            );
        }

        return (int) calculado;
    }

    private AjusteManualStockResponse resolverOperacionRepetida(
            MovimientoStock movimiento,
            Producto producto,
            Usuario usuario,
            String tipoMovimiento,
            int cantidad
    ) {
        boolean coincide = movimiento.getProducto().getIdProducto()
                .equals(producto.getIdProducto())
                && movimiento.getUsuario().getIdUsuario()
                .equals(usuario.getIdUsuario())
                && movimiento.getCantidad().equals(cantidad)
                && tipoMovimiento.equals(movimiento.getTipoMovimiento())
                && ORIGEN_AJUSTE_MANUAL.equals(movimiento.getOrigen());

        if (!coincide) {
            throw error(
                    HttpStatus.CONFLICT,
                    "La identificación de la operación ya fue utilizada"
            );
        }

        return convertirRespuesta(
                movimiento,
                "El ajuste manual ya había sido registrado"
        );
    }

    private AjusteManualStockResponse convertirRespuesta(
            MovimientoStock movimiento,
            String mensaje
    ) {
        return new AjusteManualStockResponse(
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

    private AjusteManualStockException error(
            HttpStatus status,
            String mensaje
    ) {
        return new AjusteManualStockException(status, mensaje);
    }
}
