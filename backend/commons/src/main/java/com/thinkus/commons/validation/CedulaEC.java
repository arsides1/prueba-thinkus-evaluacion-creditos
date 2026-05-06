package com.thinkus.commons.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación de Bean Validation que verifica que un String es una cédula
 * ecuatoriana válida según el algoritmo de Módulo 10.
 *
 * <p>Reglas:
 * <ul>
 *   <li>Debe tener exactamente 10 dígitos numéricos.</li>
 *   <li>Los dos primeros dígitos (provincia) deben estar entre 01 y 24.</li>
 *   <li>El tercer dígito debe ser menor que 6 (cédulas de personas naturales).</li>
 *   <li>El décimo dígito (verificador) debe coincidir con el resultado del
 *       algoritmo Módulo 10 aplicado a los nueve primeros dígitos.</li>
 * </ul>
 *
 * <p>Uso típico:
 * <pre>
 * public record EvaluacionRequest(
 *     {@literal @}CedulaEC String cedula,
 *     {@literal @}Positive BigDecimal monto,
 *     ...
 * ) {}
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CedulaECValidator.class)
public @interface CedulaEC {

    String message() default "La cédula ecuatoriana no es válida";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
