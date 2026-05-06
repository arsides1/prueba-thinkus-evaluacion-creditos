package com.thinkus.orquestador.infrastructure.adapter.out.client;

import com.thinkus.orquestador.application.port.out.RiesgosPort;
import com.thinkus.orquestador.domain.exception.EvaluacionInvalidaException;
import com.thinkus.orquestador.domain.model.DeudasCliente;
import com.thinkus.orquestador.domain.model.ScoreCrediticio;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

/**
 * Adapter que implementa el puerto RiesgosPort consumiendo el cliente REST
 * declarativo y aplicando resilience.
 *
 * Por que separar este adapter de la interfaz @RegisterRestClient:
 *  - La interfaz declara el contrato HTTP (path, params).
 *  - El adapter agrega: politica de fallos (Retry/Timeout/CircuitBreaker),
 *    conversion DTO -> dominio, fallback con valores degradados.
 *
 * Si no hubiera Fault Tolerance, una caida del MS B tumbaria el orquestador.
 * Con esto, una caida temporal lo degrada elegantemente: deudas se asume 0,
 * y para score se rechaza la solicitud (no podemos APROBAR sin score real).
 *
 * Timeout 5 segundos = 2 segundos de latencia simulada + margen para reintentos.
 */
@ApplicationScoped
public class AdaptadorClienteRiesgos implements RiesgosPort {

    private static final Logger LOG = Logger.getLogger(AdaptadorClienteRiesgos.class);

    @Inject
    @RestClient
    ClienteRestRiesgos cliente;

    @Override
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 200)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Fallback(fallbackMethod = "fallbackScore")
    public Uni<ScoreCrediticio> obtenerScore(String cedula) {
        return cliente.obtenerScore(cedula)
                .onItem().transform(dto -> new ScoreCrediticio(dto.cedula(), dto.score()));
    }

    /**
     * Si el buro no responde, no podemos saber el score. Lanzamos excepcion
     * para que el use case rechace la solicitud (mejor errar conservadoramente
     * en credito que aprobar a ciegas).
     */
    @SuppressWarnings("unused")
    Uni<ScoreCrediticio> fallbackScore(String cedula) {
        LOG.warnf("Fallback de score activado para cedula=%s. Buro no disponible.", cedula);
        return Uni.createFrom().failure(
                new EvaluacionInvalidaException(
                        "No se pudo obtener el score del buro para la cedula " + cedula));
    }

    @Override
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 200)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Fallback(fallbackMethod = "fallbackDeudas")
    public Uni<DeudasCliente> obtenerDeudas(String cedula) {
        return cliente.obtenerDeudas(cedula)
                .onItem().transform(dto -> new DeudasCliente(dto.cedula(), dto.mensualidadTotal()));
    }

    /**
     * Para deudas si podemos asumir un valor degradado: si el buro no responde,
     * tratamos al cliente como si no tuviera deudas (mas pesimista seria asumir
     * un monto alto, pero eso forzaria rechazos injustos durante caidas).
     */
    @SuppressWarnings("unused")
    Uni<DeudasCliente> fallbackDeudas(String cedula) {
        LOG.warnf("Fallback de deudas activado para cedula=%s. Asumiendo BigDecimal.ZERO.", cedula);
        return Uni.createFrom().item(new DeudasCliente(cedula, BigDecimal.ZERO));
    }
}
