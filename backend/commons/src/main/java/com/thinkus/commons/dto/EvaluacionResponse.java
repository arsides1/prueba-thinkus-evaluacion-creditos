package com.thinkus.commons.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta de la evaluación crediticia.
 *
 * <p>Devuelto por el Microservicio A al frontend tras orquestar la consulta
 * al Microservicio B y aplicar la política crediticia.
 *
 * @param id       Identificador único de la evaluación persistida.
 * @param cedula   Cédula del solicitante.
 * @param monto    Monto solicitado.
 * @param estado   Resultado final: {@code APROBADO} o {@code RECHAZADO}.
 * @param motivo   Detalle textual del motivo (especialmente útil en rechazos).
 * @param score    Score crediticio obtenido del Microservicio B (0-100).
 * @param fecha    Fecha y hora de la evaluación.
 */
public record EvaluacionResponse(
        Long id,
        String cedula,
        BigDecimal monto,
        String estado,
        String motivo,
        Integer score,
        LocalDateTime fecha
) {
}
