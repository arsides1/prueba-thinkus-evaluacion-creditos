package com.thinkus.orquestador.infrastructure.adapter.in.rest.manejador;

import com.thinkus.commons.exception.BusinessException;
import com.thinkus.orquestador.domain.exception.EvaluacionInvalidaException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Mapea excepciones a respuestas HTTP en formato Problem Details (RFC 7807).
 *
 * Mismo patron que en el ms-riesgos: api moderna @ServerExceptionMapper,
 * formato estandar para que cualquier cliente pueda parsear errores.
 */
public class ManejadorErroresGlobal {

    @ServerExceptionMapper(ConstraintViolationException.class)
    public Response cuandoValidacionFalla(ConstraintViolationException ex) {
        List<Map<String, String>> errores = ex.getConstraintViolations().stream()
                .map(v -> Map.of(
                        "campo", v.getPropertyPath().toString(),
                        "valor", String.valueOf(v.getInvalidValue()),
                        "mensaje", v.getMessage()))
                .toList();

        var body = Map.of(
                "status", 400,
                "title", "Solicitud invalida",
                "detail", "Uno o mas campos no cumplen las reglas de validacion",
                "instant", OffsetDateTime.now().toString(),
                "errores", errores);

        return Response.status(400)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    @ServerExceptionMapper(IllegalArgumentException.class)
    public Response cuandoArgumentoInvalido(IllegalArgumentException ex) {
        var body = Map.of(
                "status", 400,
                "title", "Argumento invalido",
                "detail", ex.getMessage(),
                "instant", OffsetDateTime.now().toString());
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    /** 422 Unprocessable Entity: el buro no respondio o algo similar bloquea evaluar. */
    @ServerExceptionMapper(EvaluacionInvalidaException.class)
    public Response cuandoNoSePuedeEvaluar(EvaluacionInvalidaException ex) {
        var body = Map.of(
                "status", 422,
                "title", "No se pudo procesar la evaluacion",
                "detail", ex.getMessage(),
                "instant", OffsetDateTime.now().toString());
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    @ServerExceptionMapper(BusinessException.class)
    public Response cuandoErrorDeNegocio(BusinessException ex) {
        var body = Map.of(
                "status", 422,
                "title", "Error de negocio",
                "code", ex.getCodigo(),
                "detail", ex.getMessage(),
                "instant", OffsetDateTime.now().toString());
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    @ServerExceptionMapper(Throwable.class)
    public Response cuandoFallaInesperada(Throwable ex) {
        var body = Map.of(
                "status", 500,
                "title", "Error interno",
                "detail", "Ocurrio un problema procesando la solicitud",
                "instant", OffsetDateTime.now().toString());
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
