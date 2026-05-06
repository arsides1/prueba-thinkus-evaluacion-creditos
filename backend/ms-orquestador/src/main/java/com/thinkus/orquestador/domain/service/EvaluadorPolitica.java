package com.thinkus.orquestador.domain.service;

import com.thinkus.orquestador.domain.model.DeudasCliente;
import com.thinkus.orquestador.domain.model.ResultadoEvaluacion;
import com.thinkus.orquestador.domain.model.ScoreCrediticio;
import com.thinkus.orquestador.domain.model.SolicitudCredito;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Politica crediticia: implementa la regla de negocio del enunciado.
 *
 * Regla:
 *   APROBADO si score > umbral  Y  (deudaMensual + montoSolicitado) < (salario * porcentaje)
 *
 * Esta clase es deliberadamente PURA: no tiene anotaciones CDI, no inyecta
 * nada, no llama a infraestructura. Se puede testear en JUnit sin levantar
 * Quarkus. Esta es una de las grandes ventajas de Hexagonal: el dominio se
 * prueba en milisegundos.
 *
 * Los parametros (umbral, porcentaje) llegan por argumento, no estan
 * hardcodeados. Asi la politica es configurable por ambiente.
 */
public class EvaluadorPolitica {

    private final int umbralScore;
    private final BigDecimal porcentajeCapacidadPago;

    public EvaluadorPolitica(int umbralScore, BigDecimal porcentajeCapacidadPago) {
        this.umbralScore = umbralScore;
        this.porcentajeCapacidadPago = porcentajeCapacidadPago;
    }

    public ResultadoEvaluacion evaluar(SolicitudCredito solicitud,
                                        ScoreCrediticio score,
                                        DeudasCliente deudas) {
        var ahora = LocalDateTime.now();
        var deudaMensual = deudas.mensualidadTotal();

        // Regla 1: el score debe superar el umbral
        if (!score.superaUmbral(umbralScore)) {
            return new ResultadoEvaluacion.Rechazado(
                    solicitud.cedula(),
                    solicitud.monto(),
                    solicitud.anios(),
                    solicitud.salario(),
                    score.valor(),
                    deudaMensual,
                    ahora,
                    "Score (" + score.valor() + ") no supera el umbral minimo (" + umbralScore + ")"
            );
        }

        // Regla 2: capacidad de pago. La suma de la deuda actual + el monto que se
        // pide ahora no puede superar un porcentaje del salario mensual.
        BigDecimal capacidadMaxima = solicitud.salario().multiply(porcentajeCapacidadPago);
        BigDecimal deudaTotalProyectada = deudaMensual.add(solicitud.monto());

        if (deudaTotalProyectada.compareTo(capacidadMaxima) >= 0) {
            return new ResultadoEvaluacion.Rechazado(
                    solicitud.cedula(),
                    solicitud.monto(),
                    solicitud.anios(),
                    solicitud.salario(),
                    score.valor(),
                    deudaMensual,
                    ahora,
                    "Deuda proyectada (" + deudaTotalProyectada + ") supera la capacidad de pago (" + capacidadMaxima + ")"
            );
        }

        return new ResultadoEvaluacion.Aprobado(
                solicitud.cedula(),
                solicitud.monto(),
                solicitud.anios(),
                solicitud.salario(),
                score.valor(),
                deudaMensual,
                ahora
        );
    }
}
