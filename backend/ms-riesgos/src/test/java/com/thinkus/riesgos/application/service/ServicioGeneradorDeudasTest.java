package com.thinkus.riesgos.application.service;

import com.thinkus.riesgos.domain.model.Deuda;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del generador de deudas. Lo importante aca es:
 *  - Que respete los limites de cantidad (min..max)
 *  - Que las mensualidades caigan en el rango configurado
 *  - Que la suma sea exacta (no haya error de redondeo)
 *
 * Uso @RepeatedTest porque el componente es aleatorio: una sola corrida
 * podria pasar por suerte. 30 repeticiones cubren el espacio razonablemente.
 */
@QuarkusTest
@DisplayName("ServicioGeneradorDeudas")
class ServicioGeneradorDeudasTest {

    @Inject
    ServicioGeneradorDeudas servicio;

    @RepeatedTest(30)
    @DisplayName("La cantidad generada esta entre 0 y 5")
    void cantidadEnRango() {
        List<Deuda> deudas = servicio.generar("1716123458");
        assertThat(deudas.size()).isBetween(0, 5);
    }

    @RepeatedTest(30)
    @DisplayName("Cada mensualidad cae entre 50 y 800")
    void mensualidadEnRango() {
        List<Deuda> deudas = servicio.generar("1716123458");
        for (Deuda d : deudas) {
            assertThat(d.mensualidad()).isBetween(BigDecimal.valueOf(50), BigDecimal.valueOf(800));
        }
    }

    @Test
    @DisplayName("La suma de mensualidades coincide con el reduce manual")
    void sumaConsistente() {
        // Llamamos varias veces y para cada lista verificamos la suma
        IntStream.range(0, 20).forEach(i -> {
            List<Deuda> deudas = servicio.generar("1716123458");
            BigDecimal totalEsperado = deudas.stream()
                    .map(Deuda::mensualidad)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalServicio = servicio.sumarMensualidades(deudas);

            assertThat(totalServicio.compareTo(totalEsperado))
                    .as("suma debe ser consistente para iteracion %d", i)
                    .isEqualTo(0);
        });
    }
}
