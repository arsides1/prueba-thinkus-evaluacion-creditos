package com.thinkus.commons.dto;

/**
 * DTO de respuesta del endpoint del Microservicio B que entrega el score crediticio.
 *
 * <p>El score es un valor aleatorio entre 0 y 100 según el enunciado de la prueba.
 *
 * @param cedula Cédula consultada.
 * @param score  Score crediticio (0-100).
 */
public record ScoreResponse(
        String cedula,
        Integer score
) {
}
