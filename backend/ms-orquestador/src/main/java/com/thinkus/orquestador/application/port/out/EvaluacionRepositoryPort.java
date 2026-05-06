package com.thinkus.orquestador.application.port.out;

import com.thinkus.orquestador.domain.model.ResultadoEvaluacion;
import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Puerto OUT para persistir y consultar evaluaciones.
 *
 * Igual que el RiesgosPort, el use case depende de esta abstraccion. La
 * implementacion concreta (Panache + PostgreSQL reactivo) vive en
 * infrastructure/adapter/out/persistence.
 */
public interface EvaluacionRepositoryPort {

    Uni<ResultadoEvaluacion> guardar(ResultadoEvaluacion resultado);

    Uni<List<ResultadoEvaluacion>> listarTodas();
}
