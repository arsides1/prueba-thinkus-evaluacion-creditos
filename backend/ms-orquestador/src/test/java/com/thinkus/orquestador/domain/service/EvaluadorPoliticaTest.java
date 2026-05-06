package com.thinkus.orquestador.domain.service;

import com.thinkus.orquestador.domain.model.DeudasCliente;
import com.thinkus.orquestador.domain.model.ResultadoEvaluacion;
import com.thinkus.orquestador.domain.model.ScoreCrediticio;
import com.thinkus.orquestador.domain.model.SolicitudCredito;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del EvaluadorPolitica.
 *
 * Como el dominio es puro (sin anotaciones de framework), los tests corren
 * en JUnit puro sin levantar Quarkus. Resultado: milisegundos por test
 * y feedback inmediato durante el desarrollo.
 */
@DisplayName("EvaluadorPolitica - regla de negocio del orquestador")
class EvaluadorPoliticaTest {

    private static final String CEDULA = "1716123458";
    private static final int UMBRAL_SCORE = 70;
    private static final BigDecimal PORCENTAJE = new BigDecimal("0.40");

    private final EvaluadorPolitica evaluador = new EvaluadorPolitica(UMBRAL_SCORE, PORCENTAJE);

    @Test
    @DisplayName("Aprueba cuando score>70 y deuda+monto < 40% del salario")
    void felizPathAprobado() {
        var solicitud = solicitudConMontoSalario(new BigDecimal("500"), new BigDecimal("3000"));
        var score = new ScoreCrediticio(CEDULA, 80);
        var deudas = new DeudasCliente(CEDULA, new BigDecimal("100"));
        // 100 + 500 = 600  <  3000 * 0.40 = 1200 -> APROBADO

        var resultado = evaluador.evaluar(solicitud, score, deudas);

        assertThat(resultado).isInstanceOf(ResultadoEvaluacion.Aprobado.class);
        assertThat(resultado.estado()).isEqualTo("APROBADO");
        assertThat(resultado.score()).isEqualTo(80);
    }

    @ParameterizedTest(name = "[{index}] score={0} -> rechazo por score bajo")
    @CsvSource({"0", "30", "50", "69", "70"})
    @DisplayName("Rechaza cuando score <= umbral (70)")
    void rechazoPorScoreBajo(int valorScore) {
        var solicitud = solicitudConMontoSalario(new BigDecimal("100"), new BigDecimal("5000"));
        var score = new ScoreCrediticio(CEDULA, valorScore);
        var deudas = DeudasCliente.sinDeudas(CEDULA);

        var resultado = evaluador.evaluar(solicitud, score, deudas);

        assertThat(resultado).isInstanceOf(ResultadoEvaluacion.Rechazado.class);
        assertThat(((ResultadoEvaluacion.Rechazado) resultado).razon())
                .contains("Score").contains("umbral");
    }

    @Test
    @DisplayName("Rechaza cuando deuda + monto >= salario * porcentaje")
    void rechazoPorCapacidadInsuficiente() {
        // salario 1000, capacidad max = 400. Pedimos 350 con deuda existente 100 -> 450 > 400 -> RECHAZADO
        var solicitud = solicitudConMontoSalario(new BigDecimal("350"), new BigDecimal("1000"));
        var score = new ScoreCrediticio(CEDULA, 90); // score perfecto, no es por aqui
        var deudas = new DeudasCliente(CEDULA, new BigDecimal("100"));

        var resultado = evaluador.evaluar(solicitud, score, deudas);

        assertThat(resultado).isInstanceOf(ResultadoEvaluacion.Rechazado.class);
        assertThat(((ResultadoEvaluacion.Rechazado) resultado).razon())
                .contains("capacidad de pago");
    }

    @Test
    @DisplayName("Caso borde: deuda + monto = capacidad maxima -> RECHAZADO (la regla es estrictamente menor)")
    void rechazoEnBordeIgualdad() {
        // salario 1000 * 0.40 = 400. monto + deuda = 400 -> debe rechazar (regla pide <, no <=)
        var solicitud = solicitudConMontoSalario(new BigDecimal("300"), new BigDecimal("1000"));
        var score = new ScoreCrediticio(CEDULA, 90);
        var deudas = new DeudasCliente(CEDULA, new BigDecimal("100"));

        var resultado = evaluador.evaluar(solicitud, score, deudas);

        assertThat(resultado).isInstanceOf(ResultadoEvaluacion.Rechazado.class);
    }

    @Test
    @DisplayName("Cliente sin deudas con buen score y monto razonable -> APROBADO")
    void aprobadoSinDeudas() {
        var solicitud = solicitudConMontoSalario(new BigDecimal("1000"), new BigDecimal("5000"));
        var score = new ScoreCrediticio(CEDULA, 95);
        var deudas = DeudasCliente.sinDeudas(CEDULA);

        var resultado = evaluador.evaluar(solicitud, score, deudas);

        assertThat(resultado).isInstanceOf(ResultadoEvaluacion.Aprobado.class);
    }

    private SolicitudCredito solicitudConMontoSalario(BigDecimal monto, BigDecimal salario) {
        return new SolicitudCredito(CEDULA, monto, 5, salario);
    }
}
