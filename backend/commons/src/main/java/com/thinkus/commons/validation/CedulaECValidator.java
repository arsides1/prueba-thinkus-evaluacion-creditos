package com.thinkus.commons.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador de la anotación {@link CedulaEC}.
 *
 * <p>Implementa el algoritmo de Módulo 10 oficial del Registro Civil del Ecuador
 * para validar el dígito verificador de cédulas de personas naturales.
 *
 * <p>Algoritmo paso a paso:
 * <ol>
 *   <li>Verificar que el input sea exactamente 10 dígitos numéricos.</li>
 *   <li>Verificar que los dos primeros dígitos (provincia) estén entre 01 y 24.</li>
 *   <li>Verificar que el tercer dígito sea menor que 6 (persona natural).</li>
 *   <li>Multiplicar cada uno de los primeros 9 dígitos por los coeficientes
 *       [2, 1, 2, 1, 2, 1, 2, 1, 2].</li>
 *   <li>A cada producto, si es mayor que 9, restarle 9 (equivalente a sumar sus dígitos).</li>
 *   <li>Sumar todos los resultados.</li>
 *   <li>El dígito verificador esperado es: {@code (10 - (suma % 10)) % 10}.</li>
 *   <li>Validar que coincida con el décimo dígito de la cédula.</li>
 * </ol>
 *
 * <p>Esta clase es <b>thread-safe</b> e inmutable: no mantiene estado entre validaciones.
 *
 * <p>Devuelve {@code true} si el valor es {@code null} o vacío. La obligatoriedad
 * debe manejarse con {@code @NotBlank} si corresponde.
 */
public class CedulaECValidator implements ConstraintValidator<CedulaEC, String> {

    private static final int[] COEFICIENTES = {2, 1, 2, 1, 2, 1, 2, 1, 2};
    private static final int LONGITUD_CEDULA = 10;
    private static final int PROVINCIA_MIN = 1;
    private static final int PROVINCIA_MAX = 24;
    private static final int TERCER_DIGITO_MAX = 6;

    @Override
    public boolean isValid(String cedula, ConstraintValidatorContext context) {
        // null y vacío se consideran válidos: la obligatoriedad se valida con @NotBlank
        if (cedula == null || cedula.isBlank()) {
            return true;
        }

        String trimmed = cedula.trim();

        // 1. Longitud exacta de 10 caracteres
        if (trimmed.length() != LONGITUD_CEDULA) {
            return false;
        }

        // 2. Todos los caracteres deben ser dígitos
        for (int i = 0; i < LONGITUD_CEDULA; i++) {
            if (!Character.isDigit(trimmed.charAt(i))) {
                return false;
            }
        }

        // 3. Provincia entre 01 y 24
        int provincia = Integer.parseInt(trimmed.substring(0, 2));
        if (provincia < PROVINCIA_MIN || provincia > PROVINCIA_MAX) {
            return false;
        }

        // 4. Tercer dígito menor que 6 (cédula de persona natural)
        int tercerDigito = Character.getNumericValue(trimmed.charAt(2));
        if (tercerDigito >= TERCER_DIGITO_MAX) {
            return false;
        }

        // 5. Algoritmo Módulo 10 sobre los primeros 9 dígitos
        int suma = 0;
        for (int i = 0; i < COEFICIENTES.length; i++) {
            int digito = Character.getNumericValue(trimmed.charAt(i));
            int producto = digito * COEFICIENTES[i];
            suma += (producto > 9) ? producto - 9 : producto;
        }

        // 6. Cálculo del dígito verificador esperado
        int verificadorEsperado = (10 - (suma % 10)) % 10;
        int verificadorReal = Character.getNumericValue(trimmed.charAt(9));

        return verificadorEsperado == verificadorReal;
    }
}
