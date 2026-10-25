package gestion_comercial.service.impl;

import gestion_comercial.dto.request.DescuentoClienteRequest;
import gestion_comercial.dto.response.DescuentoClienteResponse;
import gestion_comercial.entity.Cliente;
import gestion_comercial.exception.DescuentoClienteException;
import gestion_comercial.repository.ClienteRepository;
import gestion_comercial.service.interfaces.IDescuentoClienteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DescuentoClienteService
        implements IDescuentoClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public DescuentoClienteResponse actualizar(
            Integer idCliente,
            DescuentoClienteRequest request
    ) {
        if (idCliente == null || idCliente <= 0) {
            throw new DescuentoClienteException(
                    HttpStatus.NOT_FOUND,
                    "El cliente indicado no existe"
            );
        }

        Cliente cliente = clienteRepository
                .buscarPorIdParaModificarDescuento(idCliente)
                .orElseThrow(() -> new DescuentoClienteException(
                        HttpStatus.NOT_FOUND,
                        "El cliente indicado no existe"
                ));

        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new DescuentoClienteException(
                    HttpStatus.CONFLICT,
                    "No se puede modificar el descuento de un cliente inactivo"
            );
        }

        BigDecimal descuento = convertirDescuento(
                request.descuentoPorcentaje()
        );

        cliente.setDescuentoPorcentaje(descuento);
        Cliente guardado = clienteRepository.save(cliente);

        return new DescuentoClienteResponse(
                guardado.getIdCliente(),
                guardado.getNombre(),
                guardado.getApellido(),
                guardado.getDescuentoPorcentaje(),
                guardado.getDescuentoPorcentaje() == null
                        ? "Descuento quitado correctamente"
                        : "Descuento actualizado correctamente"
        );
    }

    private BigDecimal convertirDescuento(
            String valor
    ) {
        if (valor == null || valor.isEmpty()) {
            return null;
        }

        BigDecimal descuento = new BigDecimal(valor);

        if (descuento.compareTo(BigDecimal.ZERO) < 0
                || descuento.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(
                    "El descuento debe estar entre 0 y 100"
            );
        }

        return descuento;
    }
}
