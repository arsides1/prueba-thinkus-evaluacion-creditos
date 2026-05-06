package com.thinkus.commons.dto;

import java.math.BigDecimal;

/**
 * Ítem individual de deuda dentro de la respuesta del Microservicio B.
 *
 * @param descripcion Descripción de la deuda (ej. "Tarjeta de crédito Banco X").
 * @param mensualidad Cuota mensual de la deuda.
 */
public record DeudaItem(
        String descripcion,
        BigDecimal mensualidad
) {
}
