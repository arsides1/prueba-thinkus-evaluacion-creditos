package com.thinkus.commons.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios del {@link CedulaECValidator}.
 *
 * <p>Cubre el algoritmo de Módulo 10 con cédulas reales válidas y casos
 * inválidos representativos: longitud, provincia, tercer dígito y dígito
 * verificador incorrecto.
 */
@DisplayName("CedulaECValidator - Algoritmo Módulo 10")
class CedulaECValidatorTest {

    private final CedulaECValidator validator = new CedulaECValidator();

    @ParameterizedTest(name = "[{index}] cédula válida: {0}")
    @ValueSource(strings = {
            "1716123458",  // provincia 17, calculada con módulo 10
            "0910123454",  // provincia 09, calculada con módulo 10
            "0500000005",  // provincia 05, suma=5, verificador=5
            "0500000500"   // provincia 05, suma=10, verificador=0 (caso múltiplo de 10)
    })
    @DisplayName("Cédulas válidas pasan el algoritmo")
    void cedulasValidas(String cedula) {
        assertThat(validator.isValid(cedula, null)).isTrue();
    }

    @ParameterizedTest(name = "[{index}] {0}: {1}")
    @org.junit.jupiter.params.provider.CsvSource({
            "longitud menor a 10,    123456789",
            "longitud mayor a 10,    12345678901",
            "contiene letras,        171612345A",
            "provincia 25 inválida,  2516123458",
            "provincia 30 inválida,  3016123458",
            "provincia 00 inválida,  0016123458",
            "tercer dígito = 6,      1766123458",
            "tercer dígito = 9,      1796123458",
            "dígito verificador err, 1716123459"
    })
    @DisplayName("Cédulas inválidas son rechazadas")
    void cedulasInvalidas(String descripcion, String cedula) {
        assertThat(validator.isValid(cedula, null))
                .as(descripcion)
                .isFalse();
    }

    @ParameterizedTest(name = "[{index}] valor blanco/null: {0}")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("null, vacío y blanco se consideran válidos (delegado a @NotBlank)")
    void nullOVacio(String valor) {
        assertThat(validator.isValid(valor, null)).isTrue();
    }
}
