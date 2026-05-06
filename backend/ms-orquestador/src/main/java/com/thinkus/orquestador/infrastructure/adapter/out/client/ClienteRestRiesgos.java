package com.thinkus.orquestador.infrastructure.adapter.out.client;

import com.thinkus.commons.dto.DeudasResponse;
import com.thinkus.commons.dto.ScoreResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Interfaz declarativa del cliente REST hacia el Microservicio B.
 *
 * configKey="riesgos-api" debe coincidir con la propiedad
 * quarkus.rest-client.riesgos-api.url en application.properties.
 *
 * Esta interfaz NO es el adapter del puerto. El adapter (AdaptadorClienteRiesgos)
 * la inyecta y le anade resilience (Retry, Timeout, CircuitBreaker, Fallback)
 * y conversion al modelo de dominio. Mantener la interfaz "limpia" aqui
 * facilita testear el adapter mockeandola.
 */
@RegisterRestClient(configKey = "riesgos-api")
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/riesgos")
public interface ClienteRestRiesgos {

    @GET
    @Path("/score/{cedula}")
    Uni<ScoreResponse> obtenerScore(@PathParam("cedula") String cedula);

    @GET
    @Path("/deudas/{cedula}")
    Uni<DeudasResponse> obtenerDeudas(@PathParam("cedula") String cedula);
}
