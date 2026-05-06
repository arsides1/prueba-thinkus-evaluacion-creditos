package com.thinkus.commons.dto;

import com.thinkus.commons.validation.CedulaEC;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO de entrada para la evaluación crediticia.
 *
 * <p>Recibido por el Microservicio A en {@code POST /v1/credit-evaluations}.
 *
 * <p>Validaciones aplicadas mediante Bean Validation:
 * <ul>
 *   <li>{@code cedula}: Cédula ecuatoriana válida (algoritmo Módulo 10).</li>
 *   <li>{@code monto}: Positivo y mayor que cero.</li>
 *   <li>{@code anios}: Entero positivo entre 1 y 30.</li>
 *   <li>{@code salario}: Positivo y mayor que cero.</li>
 * </ul>
 *
 * @param cedula  Cédula del solicitante (10 dígitos numéricos).
 * @param monto   Monto solicitado de crédito.
 * @param anios   Plazo del crédito en años.
 * @param salario Salario mensual declarado por el solicitante.
 */
public record EvaluacionRequest(
        @CedulaEC
        @NotNull
        String cedula,

        @NotNull
        @Positive
        BigDecimal monto,

        @NotNull
        @Min(1)
        Integer anios,

        @NotNull
        @Positive
        BigDecimal salario
) {
}
