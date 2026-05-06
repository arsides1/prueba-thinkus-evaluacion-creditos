package com.thinkus.riesgos.infrastructure.adapter.in.rest;

import com.thinkus.commons.dto.DeudaItem;
import com.thinkus.commons.dto.DeudasResponse;
import com.thinkus.commons.dto.ScoreResponse;
import com.thinkus.commons.validation.CedulaEC;
import com.thinkus.riesgos.application.service.ServicioGeneradorDeudas;
import com.thinkus.riesgos.application.service.ServicioGeneradorScore;
import com.thinkus.riesgos.domain.model.Deuda;
import com.thinkus.riesgos.domain.model.Score;
import com.thinkus.riesgos.infrastructure.config.ConfiguracionApp;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Endpoints del servicio mock de riesgos.
 *
 * Por que @RunOnVirtualThread y no Mutiny aca:
 *  - El servicio solo simula latencia con Thread.sleep y devuelve datos random.
 *  - No hay nada que componer reactivamente, ni llamadas paralelas, ni I/O real.
 *  - Codigo bloqueante imperativo es muchisimo mas legible para este caso.
 *  - Cada request corre en su propio Virtual Thread (Java 21+); el sleep
 *    libera el carrier thread, asi que el throughput se mantiene alto.
 *
 * La cedula se valida en el path con @CedulaEC (anotacion del modulo commons).
 * Si llega invalida, Hibernate Validator devuelve 400 antes de entrar al metodo.
 */
@Path("/v1/riesgos")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Riesgos", description = "Consulta de score crediticio y deudas (mock)")
public class RecursoRiesgos {

    private static final Logger LOG = Logger.getLogger(RecursoRiesgos.class);

    @Inject
    ServicioGeneradorScore generadorScore;

    @Inject
    ServicioGeneradorDeudas generadorDeudas;

    @Inject
    ConfiguracionApp config;

    @GET
    @Path("/score/{cedula}")
    @RunOnVirtualThread
    @Operation(
            summary = "Obtiene el score crediticio para una cedula",
            description = "Devuelve un score aleatorio entre 0 y 100. " +
                    "Simula una latencia de 2 segundos para reproducir un buro real."
    )
    @APIResponse(responseCode = "200", description = "Score generado",
            content = @Content(schema = @Schema(implementation = ScoreResponse.class)))
    @APIResponse(responseCode = "400", description = "Cedula invalida")
    public ScoreResponse obtenerScore(@PathParam("cedula") @Valid @CedulaEC String cedula) {
        LOG.debugf("Score solicitado para cedula=%s", cedula);

        dormir(config.latencia().scoreMs());

        Score score = generadorScore.generar(cedula);
        return new ScoreResponse(score.cedula(), score.valor());
    }

    @GET
    @Path("/deudas/{cedula}")
    @RunOnVirtualThread
    @Operation(
            summary = "Obtiene las deudas y mensualidades para una cedula",
            description = "Devuelve entre 0 y 5 deudas con mensualidad aleatoria. " +
                    "Incluye la suma total de mensualidades. Latencia simulada: 1.5 segundos."
    )
    @APIResponse(responseCode = "200", description = "Lista de deudas (puede ser vacia)",
            content = @Content(schema = @Schema(implementation = DeudasResponse.class)))
    @APIResponse(responseCode = "400", description = "Cedula invalida")
    public DeudasResponse obtenerDeudas(@PathParam("cedula") @Valid @CedulaEC String cedula) {
        LOG.debugf("Deudas solicitadas para cedula=%s", cedula);

        dormir(config.latencia().deudasMs());

        List<Deuda> deudas = generadorDeudas.generar(cedula);

        // Convertimos del modelo de dominio al DTO compartido en commons.
        // No exponemos directo el record de dominio porque el contrato API
        // y el modelo interno deben poder evolucionar por separado.
        var items = deudas.stream()
                .map(d -> new DeudaItem(d.descripcion(), d.mensualidad()))
                .toList();

        return new DeudasResponse(cedula, items, generadorDeudas.sumarMensualidades(deudas));
    }

    /**
     * Sleep envuelto en un metodo aparte solo para mantener el handler limpio
     * y poder cambiar la estrategia (ej. usar metricas) sin tocar los endpoints.
     */
    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // recuperar el flag para no perder la senial
            throw new RuntimeException("Interrumpido durante simulacion de latencia", e);
        }
    }
}
