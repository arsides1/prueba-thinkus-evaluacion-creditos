package com.thinkus.orquestador.infrastructure.adapter.in.rest;

import com.thinkus.commons.dto.EvaluacionRequest;
import com.thinkus.commons.dto.EvaluacionResponse;
import com.thinkus.orquestador.application.port.in.EvaluarCredito;
import com.thinkus.orquestador.domain.model.SolicitudCredito;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Recurso REST principal del orquestador.
 *
 * POST /v1/credit-evaluations  -> dispara la evaluacion crediticia completa
 * GET  /v1/credit-evaluations  -> lista el historial (para la UI de listado)
 *
 * Devuelve Uni<Response> para que el endpoint sea reactivo end-to-end.
 * Quarkus suscribe al Uni automaticamente; no hay que llamar subscribe().
 *
 * El handler es deliberadamente delgado: convierte DTO -> dominio, llama al
 * use case, convierte resultado -> DTO. Toda la logica vive en el use case.
 */
@Path("/v1/credit-evaluations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Evaluaciones", description = "Evaluacion crediticia (BFF principal)")
public class RecursoEvaluaciones {

    private static final Logger LOG = Logger.getLogger(RecursoEvaluaciones.class);

    @Inject
    EvaluarCredito useCase;

    @POST
    @Operation(
            summary = "Evalua una solicitud de credito",
            description = "Recibe los datos del solicitante, consulta el buro de riesgo " +
                    "(score y deudas en paralelo), aplica la politica crediticia y persiste " +
                    "el resultado. Tarda aproximadamente 2 segundos por las latencias del buro."
    )
    @APIResponse(responseCode = "201", description = "Evaluacion creada",
            content = @Content(schema = @Schema(implementation = EvaluacionResponse.class)))
    @APIResponse(responseCode = "400", description = "Datos invalidos (cedula mal formada, montos no positivos, etc.)")
    @APIResponse(responseCode = "422", description = "No se pudo evaluar (buro no disponible)")
    public Uni<Response> evaluar(@Valid EvaluacionRequest req) {
        LOG.debugf("POST /v1/credit-evaluations | cedula=%s, monto=%s", req.cedula(), req.monto());

        var solicitud = new SolicitudCredito(req.cedula(), req.monto(), req.anios(), req.salario());

        return useCase.evaluar(solicitud)
                .onItem().transform(resultado -> {
                    var body = new EvaluacionResponse(
                            null, // el id lo asigna BD; la respuesta no lo necesita ahora
                            resultado.cedula(),
                            resultado.monto(),
                            resultado.estado(),
                            resultado.motivo(),
                            resultado.score(),
                            resultado.fecha()
                    );
                    return Response.status(Response.Status.CREATED).entity(body).build();
                });
    }

    @GET
    @Operation(
            summary = "Lista el historial completo de evaluaciones",
            description = "Devuelve todas las evaluaciones persistidas, ordenadas por fecha descendente."
    )
    @APIResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = EvaluacionResponse.class, type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY)))
    public Uni<List<EvaluacionResponse>> listar() {
        return useCase.listarHistorial()
                .onItem().transform(list -> list.stream()
                        .map(r -> new EvaluacionResponse(
                                null, r.cedula(), r.monto(), r.estado(),
                                r.motivo(), r.score(), r.fecha()))
                        .toList());
    }
}
