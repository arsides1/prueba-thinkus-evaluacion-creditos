package com.thinkus.orquestador.domain.model;

import java.math.BigDecimal;

/**
 * Solicitud de evaluacion crediticia entrante.
 *
 * Se construye a partir del DTO de la API en la capa de adapter, lo que
 * mantiene el dominio limpio de anotaciones de Bean Validation o Jackson.
 *
 * Las invariantes basicas se chequean aqui igual: defensa en profundidad.
 * La cedula ya viene validada por @CedulaEC en el DTO, pero el dominio
 * no se fia ciegamente del input.
 */
public record SolicitudCredito(
        String cedula,
        BigDecimal monto,
        int anios,
        BigDecimal salario
) {

    public SolicitudCredito {
        if (cedula == null || cedula.isBlank()) {
            throw new IllegalArgumentException("Cedula requerida");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("Monto debe ser positivo");
        }
        if (anios <= 0) {
            throw new IllegalArgumentException("Anios debe ser positivo");
        }
        if (salario == null || salario.signum() <= 0) {
            throw new IllegalArgumentException("Salario debe ser positivo");
        }
    }
}
