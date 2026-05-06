package com.thinkus.orquestador.application.port.in;

import com.thinkus.orquestador.domain.model.ResultadoEvaluacion;
import com.thinkus.orquestador.domain.model.SolicitudCredito;
import io.smallrye.mutiny.Uni;

import java.util.List;

/**
 * Puerto IN: contrato del caso de uso principal.
 *
 * El recurso REST (adapter de entrada) depende de esta interfaz, no de la
 * implementacion concreta. Esto permite testear el handler con un mock del
 * use case, y el use case con mocks de los puertos OUT.
 */
public interface EvaluarCredito {

    Uni<ResultadoEvaluacion> evaluar(SolicitudCredito solicitud);

    Uni<List<ResultadoEvaluacion>> listarHistorial();
}
