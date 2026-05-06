package com.thinkus.orquestador.application.usecase;

import com.thinkus.orquestador.application.port.in.EvaluarCredito;
import com.thinkus.orquestador.application.port.out.EvaluacionRepositoryPort;
import com.thinkus.orquestador.application.port.out.RiesgosPort;
import com.thinkus.orquestador.domain.model.ResultadoEvaluacion;
import com.thinkus.orquestador.domain.model.SolicitudCredito;
import com.thinkus.orquestador.domain.service.EvaluadorPolitica;
import com.thinkus.orquestador.infrastructure.config.ConfiguracionPolitica;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Implementacion del caso de uso de evaluacion crediticia.
 *
 * AQUI VIVE EL CORAZON DEL BFF: orquesta las dos llamadas paralelas al MS B,
 * aplica la politica de negocio y persiste el resultado.
 *
 * El metodo evaluar es la implementacion completa del flujo:
 *   1. Disparar en PARALELO la consulta de score y la consulta de deudas
 *   2. Cuando ambas resuelven, instanciar el dominio y aplicar la politica
 *   3. Persistir el resultado
 *   4. Devolver el resultado al adapter REST
 *
 * El truco clave: Uni.combine().all().unis(uScore, uDeudas) suscribe a ambos
 * Unis al mismo tiempo, no en serie. Si cada uno tarda 2.0s y 1.5s respectivamente,
 * el tiempo total es ~2.0s (el mayor de los dos), no 3.5s. Ese es el
 * gran ahorro que justifica usar Mutiny aqui.
 */
@ApplicationScoped
public class EvaluarCreditoUseCaseImpl implements EvaluarCredito {

    private static final Logger LOG = Logger.getLogger(EvaluarCreditoUseCaseImpl.class);

    @Inject
    RiesgosPort riesgos;

    @Inject
    EvaluacionRepositoryPort repositorio;

    @Inject
    ConfiguracionPolitica configPolitica;

    @Override
    public Uni<ResultadoEvaluacion> evaluar(SolicitudCredito solicitud) {
        LOG.debugf("Evaluando solicitud: cedula=%s, monto=%s, anios=%d, salario=%s",
                solicitud.cedula(), solicitud.monto(), solicitud.anios(), solicitud.salario());

        var uScore = riesgos.obtenerScore(solicitud.cedula());
        var uDeudas = riesgos.obtenerDeudas(solicitud.cedula());

        // Suscripcion paralela: ambas llamadas viajan al mismo tiempo
        return Uni.combine().all().unis(uScore, uDeudas)
                .with((score, deudas) -> {
                    var evaluador = new EvaluadorPolitica(
                            configPolitica.umbralScore(),
                            configPolitica.porcentajeCapacidadPago());
                    return evaluador.evaluar(solicitud, score, deudas);
                })
                .onItem().invoke(r -> LOG.debugf("Resultado: %s para cedula=%s", r.estado(), r.cedula()))
                .onItem().transformToUni(repositorio::guardar);
    }

    @Override
    public Uni<List<ResultadoEvaluacion>> listarHistorial() {
        return repositorio.listarTodas();
    }
}
