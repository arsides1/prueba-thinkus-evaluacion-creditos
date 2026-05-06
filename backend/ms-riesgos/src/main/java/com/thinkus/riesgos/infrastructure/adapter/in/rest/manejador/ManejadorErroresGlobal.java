package com.thinkus.riesgos.infrastructure.adapter.in.rest.manejador;

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
 * Es la forma estandar moderna de devolver errores de API: esquema fijo
 * con status, title, detail, instance. Cualquier cliente puede parsearlo
 * sin saber del dominio.
 *
 * @ServerExceptionMapper es la API moderna de RESTEasy Reactive (Quarkus 3+),
 * mas concisa que el viejo @Provider + ExceptionMapper.
 */
public class ManejadorErroresGlobal {

    /** Errores de validacion de Bean Validation -> 400 con detalle de cada campo. */
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
                "errores", errores
        );

        return Response.status(400)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    /** IllegalArgumentException viene casi siempre del dominio (records que validan en su compact constructor). */
    @ServerExceptionMapper(IllegalArgumentException.class)
    public Response cuandoArgumentoInvalido(IllegalArgumentException ex) {
        var body = Map.of(
                "status", 400,
                "title", "Argumento invalido",
                "detail", ex.getMessage(),
                "instant", OffsetDateTime.now().toString()
        );
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    /** Catch-all para errores inesperados. No expone detalles internos al cliente. */
    @ServerExceptionMapper(Throwable.class)
    public Response cuandoFallaInesperada(Throwable ex) {
        var body = Map.of(
                "status", 500,
                "title", "Error interno",
                "detail", "Ocurrio un problema procesando la solicitud",
                "instant", OffsetDateTime.now().toString()
        );
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
