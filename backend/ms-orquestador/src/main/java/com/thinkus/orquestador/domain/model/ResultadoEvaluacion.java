package com.thinkus.orquestador.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resultado de aplicar la politica crediticia a una solicitud.
 *
 * Es una sealed interface con dos implementaciones posibles. El compilador
 * obliga a cubrir ambos casos en cualquier switch exhaustivo, asi que si
 * manana se anade un tercer estado (por ejemplo "Pendiente de revision"),
 * todos los lugares que hacen pattern matching avisan en compilacion.
 *
 * Esta es una de las features mas potentes de Java 21 para modelar
 * dominios con estados acotados (Algebraic Data Types).
 *
 * Conserva los datos de la solicitud original (anios, salario) ademas del
 * resultado para que el historial persistido sea autoexplicativo.
 */
public sealed interface ResultadoEvaluacion
        permits ResultadoEvaluacion.Aprobado, ResultadoEvaluacion.Rechazado {

    String cedula();
    BigDecimal monto();
    int anios();
    BigDecimal salario();
    int score();
    BigDecimal deudaMensual();
    LocalDateTime fecha();

    default String estado() {
        return switch (this) {
            case Aprobado a -> "APROBADO";
            case Rechazado r -> "RECHAZADO";
        };
    }

    default String motivo() {
        return switch (this) {
            case Aprobado a -> "Cumple con score y capacidad de pago";
            case Rechazado r -> r.razon();
        };
    }

    record Aprobado(
            String cedula,
            BigDecimal monto,
            int anios,
            BigDecimal salario,
            int score,
            BigDecimal deudaMensual,
            LocalDateTime fecha
    ) implements ResultadoEvaluacion {}

    record Rechazado(
            String cedula,
            BigDecimal monto,
            int anios,
            BigDecimal salario,
            int score,
            BigDecimal deudaMensual,
            LocalDateTime fecha,
            String razon
    ) implements ResultadoEvaluacion {}
}
