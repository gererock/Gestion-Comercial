package gestion_comercial.service.impl;

import gestion_comercial.dto.response.MovimientoStockResponse;
import gestion_comercial.entity.MovimientoStock;
import gestion_comercial.entity.Usuario;
import gestion_comercial.exception.ConsultaMovimientoStockException;
import gestion_comercial.repository.MovimientoStockRepository;
import gestion_comercial.service.interfaces.IMovimientoStockConsultaService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MovimientoStockConsultaService
        implements IMovimientoStockConsultaService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "ENTRADA",
            "SALIDA",
            "AJUSTE_POSITIVO",
            "AJUSTE_NEGATIVO",
            "DEVOLUCION",
            "RESERVA",
            "LIBERACION_RESERVA"
    );

    private static final Set<String> ORIGENES_PERMITIDOS = Set.of(
            "VENTA_LOCAL",
            "PEDIDO_ONLINE",
            "REPOSICION_STOCK",
            "AJUSTE_MANUAL",
            "DEVOLUCION",
            "RESERVA_PEDIDO"
    );

    private static final Set<String> TIPOS_INCREMENTO = Set.of(
            "ENTRADA",
            "AJUSTE_POSITIVO",
            "DEVOLUCION",
            "LIBERACION_RESERVA"
    );

    private static final Set<String> TIPOS_REDUCCION = Set.of(
            "SALIDA",
            "AJUSTE_NEGATIVO",
            "RESERVA"
    );

    private static final LocalDateTime FECHA_NEUTRA =
            LocalDateTime.of(2000, 1, 1, 0, 0);

    private final MovimientoStockRepository movimientoStockRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoStockResponse> consultar(
            String idProducto,
            String tipoMovimiento,
            String origen,
            String fechaDesde,
            String fechaHasta
    ) {
        Integer producto = convertirIdProducto(idProducto);
        String tipo = convertirOpcion(
                tipoMovimiento,
                TIPOS_PERMITIDOS,
                "El tipo de movimiento seleccionado no es válido"
        );
        String origenNormalizado = convertirOpcion(
                origen,
                ORIGENES_PERMITIDOS,
                "El origen seleccionado no es válido"
        );
        LocalDate desde = convertirFecha(fechaDesde, "La fecha desde no es válida");
        LocalDate hasta = convertirFecha(fechaHasta, "La fecha hasta no es válida");

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La fecha desde no puede ser posterior a la fecha hasta"
            );
        }

        LocalDateTime inicio = desde == null ? null : desde.atStartOfDay();
        LocalDateTime finExclusivo = hasta == null
                ? null
                : hasta.plusDays(1).atStartOfDay();

        return movimientoStockRepository.consultar(
                        producto,
                        tipo,
                        origenNormalizado,
                        inicio != null,
                        inicio == null ? FECHA_NEUTRA : inicio,
                        finExclusivo != null,
                        finExclusivo == null ? FECHA_NEUTRA : finExclusivo
                )
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    private Integer convertirIdProducto(String valor) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }

        if (valor.isBlank() || !valor.matches("^[1-9][0-9]*$")) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El ID del producto debe ser un número entero mayor a cero"
            );
        }

        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException exception) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El ID del producto supera el límite permitido"
            );
        }
    }

    private String convertirOpcion(
            String valor,
            Set<String> permitidos,
            String mensaje
    ) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }

        if (valor.isBlank()) {
            throw error(HttpStatus.BAD_REQUEST, mensaje);
        }

        String normalizado = valor.trim().toUpperCase();

        if (!permitidos.contains(normalizado)) {
            throw error(HttpStatus.BAD_REQUEST, mensaje);
        }

        return normalizado;
    }

    private LocalDate convertirFecha(String valor, String mensaje) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }

        if (valor.isBlank()) {
            throw error(HttpStatus.BAD_REQUEST, mensaje);
        }

        try {
            return LocalDate.parse(valor);
        } catch (DateTimeException exception) {
            throw error(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    private MovimientoStockResponse convertirRespuesta(
            MovimientoStock movimiento
    ) {
        if (movimiento.getProducto() == null) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Existe un movimiento sin producto asociado"
            );
        }

        if (movimiento.getUsuario() == null) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Existe un movimiento sin usuario asociado"
            );
        }

        validarCoherencia(movimiento);

        Usuario usuario = movimiento.getUsuario();
        String nombreUsuario = (
                (usuario.getNombre() == null ? "" : usuario.getNombre())
                        + " "
                        + (usuario.getApellido() == null ? "" : usuario.getApellido())
        ).trim();

        if (nombreUsuario.isEmpty()) {
            nombreUsuario = usuario.getEmail();
        }

        return new MovimientoStockResponse(
                movimiento.getIdMovimientoStock(),
                movimiento.getProducto().getIdProducto(),
                movimiento.getProducto().getNombre(),
                movimiento.getProducto().getEstado(),
                usuario.getIdUsuario(),
                nombreUsuario,
                usuario.getEmail(),
                movimiento.getTipoMovimiento(),
                movimiento.getOrigen(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockPosterior(),
                movimiento.getFecha()
        );
    }

    private void validarCoherencia(MovimientoStock movimiento) {
        Integer cantidad = movimiento.getCantidad();
        Integer anterior = movimiento.getStockAnterior();
        Integer posterior = movimiento.getStockPosterior();
        String tipo = movimiento.getTipoMovimiento();

        if (cantidad == null || cantidad <= 0
                || anterior == null || anterior < 0
                || posterior == null || posterior < 0
                || tipo == null) {
            throw movimientoInconsistente(movimiento);
        }

        long esperado;

        if (TIPOS_INCREMENTO.contains(tipo)) {
            esperado = (long) anterior + cantidad;
        } else if (TIPOS_REDUCCION.contains(tipo)) {
            esperado = (long) anterior - cantidad;
        } else {
            throw movimientoInconsistente(movimiento);
        }

        if (esperado != posterior) {
            throw movimientoInconsistente(movimiento);
        }
    }

    private ConsultaMovimientoStockException movimientoInconsistente(
            MovimientoStock movimiento
    ) {
        return error(
                HttpStatus.CONFLICT,
                "El movimiento de stock #"
                        + movimiento.getIdMovimientoStock()
                        + " contiene datos inconsistentes"
        );
    }

    private ConsultaMovimientoStockException error(
            HttpStatus status,
            String mensaje
    ) {
        return new ConsultaMovimientoStockException(status, mensaje);
    }
}
