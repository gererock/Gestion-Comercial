package gestion_comercial.service.impl;

import gestion_comercial.dto.response.ConsultaGastosResponse;
import gestion_comercial.dto.response.GastoConsultaItemResponse;
import gestion_comercial.entity.CategoriaGasto;
import gestion_comercial.entity.Gasto;
import gestion_comercial.entity.MedioPago;
import gestion_comercial.entity.PeriodoConsultaGasto;
import gestion_comercial.exception.ConsultaGastoException;
import gestion_comercial.repository.CategoriaGastoRepository;
import gestion_comercial.repository.GastoRepository;
import gestion_comercial.repository.MedioPagoRepository;
import gestion_comercial.service.interfaces.IConsultaGastoService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ConsultaGastoService
        implements IConsultaGastoService {

    private final GastoRepository gastoRepository;

    private final CategoriaGastoRepository categoriaGastoRepository;

    private final MedioPagoRepository medioPagoRepository;

    private static final ZoneId ZONA_HORARIA = ZoneId.of(
            "America/Argentina/Cordoba");

    @Override
    @Transactional(readOnly = true)
    public ConsultaGastosResponse consultar(

            PeriodoConsultaGasto periodo,

            LocalDate desde,

            LocalDate hasta,

            Integer idCategoriaGasto,

            Integer idMedioPago) {
        PeriodoConsultaGasto periodoSeleccionado;

        if (periodo != null) {

            periodoSeleccionado = periodo;

        } else if (desde != null
                ||
                hasta != null) {

            periodoSeleccionado = PeriodoConsultaGasto.RANGO;

        } else {

            periodoSeleccionado = PeriodoConsultaGasto.MES;
        }

        LocalDate[] fechas = resolverFechas(
                periodoSeleccionado,
                desde,
                hasta);

        LocalDate fechaDesde = fechas[0];

        LocalDate fechaHasta = fechas[1];

        validarRango(
                fechaDesde,
                fechaHasta);

        validarCategoria(
                idCategoriaGasto);

        validarMedioPago(
                idMedioPago);

        List<Gasto> gastos = gastoRepository
                .buscarPorFiltros(
                        fechaDesde,
                        fechaHasta,
                        idCategoriaGasto,
                        idMedioPago);

        List<GastoConsultaItemResponse> respuestas = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        for (Gasto gasto : gastos) {

            respuestas.add(
                    convertirGasto(
                            gasto));

            total = total.add(
                    gasto.getMonto());
        }

        return new ConsultaGastosResponse(

                fechaDesde,

                fechaHasta,

                respuestas.size(),

                total,

                respuestas);
    }

    private LocalDate[] resolverFechas(

            PeriodoConsultaGasto periodo,

            LocalDate desde,

            LocalDate hasta) {

        LocalDate hoy = LocalDate.now(
                ZONA_HORARIA);

        if (periodo == PeriodoConsultaGasto.DIA) {

            return new LocalDate[] {
                    hoy,
                    hoy
            };
        }

        if (periodo == PeriodoConsultaGasto.SEMANA) {

            LocalDate inicioSemana = hoy.with(
                    TemporalAdjusters
                            .previousOrSame(
                                    DayOfWeek.MONDAY));

            LocalDate finSemana = inicioSemana.plusDays(
                    6);

            return new LocalDate[] {
                    inicioSemana,
                    finSemana
            };
        }

        if (periodo == PeriodoConsultaGasto.MES) {

            LocalDate inicioMes = hoy.withDayOfMonth(
                    1);

            LocalDate finMes = hoy.withDayOfMonth(
                    hoy.lengthOfMonth());

            return new LocalDate[] {
                    inicioMes,
                    finMes
            };
        }

        if (desde == null
                &&
                hasta == null) {

            LocalDate inicioMes = hoy.withDayOfMonth(
                    1);

            LocalDate finMes = hoy.withDayOfMonth(
                    hoy.lengthOfMonth());

            return new LocalDate[] {
                    inicioMes,
                    finMes
            };
        }

        return new LocalDate[] {
                desde,
                hasta
        };
    }

    private void validarRango(
            LocalDate desde,
            LocalDate hasta) {

        if (desde != null
                &&
                hasta != null
                &&
                desde.isAfter(
                        hasta)) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La fecha desde no puede ser posterior a la fecha hasta");
        }
    }

    private void validarCategoria(
            Integer idCategoriaGasto) {

        if (idCategoriaGasto == null) {

            return;
        }

        if (idCategoriaGasto <= 0) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La categoría de gasto indicada no es válida");
        }

        CategoriaGasto categoria = categoriaGastoRepository
                .findById(
                        idCategoriaGasto)
                .orElseThrow(
                        () -> error(
                                HttpStatus.NOT_FOUND,
                                "La categoría de gasto indicada no existe"));
    }

    private void validarMedioPago(
            Integer idMedioPago) {

        if (idMedioPago == null) {

            return;
        }

        if (idMedioPago <= 0) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El medio de pago indicado no es válido");
        }

        MedioPago medioPago = medioPagoRepository
                .findById(
                        idMedioPago)
                .orElseThrow(
                        () -> error(
                                HttpStatus.NOT_FOUND,
                                "El medio de pago indicado no existe"));
    }

    private GastoConsultaItemResponse convertirGasto(
            Gasto gasto) {

        MedioPago medioPago = gasto.getMedioPago();

        String nombreUsuario = gasto.getUsuario()
                .getNombre();

        if (gasto.getUsuario()
                .getApellido() != null
                &&
                !gasto.getUsuario()
                        .getApellido()
                        .isBlank()) {

            nombreUsuario += " "
                    +
                    gasto.getUsuario()
                            .getApellido();
        }

        return new GastoConsultaItemResponse(

                gasto.getIdGasto(),

                gasto.getFecha(),

                gasto.getCategoriaGasto()
                        .getIdCategoriaGasto(),

                gasto.getCategoriaGasto()
                        .getNombre(),

                gasto.getDescripcion(),

                gasto.getMonto(),

                medioPago != null
                        ? medioPago.getIdMedioPago()
                        : null,

                medioPago != null
                        ? medioPago.getNombre()
                        : null,

                gasto.getUsuario()
                        .getIdUsuario(),

                nombreUsuario,

                gasto.getObservaciones());
    }

    private ConsultaGastoException error(
            HttpStatus status,
            String mensaje) {

        return new ConsultaGastoException(
                status,
                mensaje);
    }
}