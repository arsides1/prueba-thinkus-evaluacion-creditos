package com.thinkus.commons.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta del endpoint del Microservicio B que entrega las deudas
 * del solicitante con sus mensualidades.
 *
 * @param cedula                  Cédula consultada.
 * @param deudas                  Lista de deudas individuales con su mensualidad.
 * @param mensualidadTotal        Suma de las mensualidades (calculada por el MS B
 *                                para evitar redondeo en el cliente).
 */
public record DeudasResponse(
        String cedula,
        List<DeudaItem> deudas,
        BigDecimal mensualidadTotal
) {
}
