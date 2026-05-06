package com.thinkus.orquestador.infrastructure.adapter.out.persistence;

import com.thinkus.orquestador.application.port.out.EvaluacionRepositoryPort;
import com.thinkus.orquestador.domain.model.ResultadoEvaluacion;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;

/**
 * Adapter que persiste evaluaciones usando Hibernate Reactive Panache.
 *
 * Implementa EvaluacionRepositoryPort, asi el use case lo inyecta como
 * abstraccion y no se entera de que abajo hay Postgres y JPA.
 *
 * Notas tecnicas:
 *  - @WithTransaction abre/commitea la sesion reactiva en torno al metodo.
 *  - @WithSession solo abre sesion (sin transaccion) para lecturas.
 *  - Se usa un PanacheRepository inyectado, no Active Record. Beneficio:
 *    la entidad es un POJO y el repositorio se puede mockear en tests
 *    del use case.
 *
 * Mapping a ResultadoEvaluacion:
 *  - El estado guardado como String se reconstruye usando el sealed pattern
 *    matching de Java 21. Si manana se anade un nuevo estado al sealed,
 *    el switch obliga a actualizar este metodo (verificacion en compilacion).
 */
@ApplicationScoped
public class AdaptadorRepositorioEvaluaciones implements EvaluacionRepositoryPort {

    @Inject
    RepositorioPanacheEvaluaciones repo;

    @Override
    @WithTransaction
    public Uni<ResultadoEvaluacion> guardar(ResultadoEvaluacion resultado) {
        var entity = aEntity(resultado);
        return repo.persist(entity)
                .onItem().transform(this::aDominio);
    }

    @Override
    @WithSession
    public Uni<List<ResultadoEvaluacion>> listarTodas() {
        return repo.listAll(Sort.by("fecha", Sort.Direction.Descending))
                .onItem().transform(lista -> lista.stream().map(this::aDominio).toList());
    }

    private EvaluacionEntity aEntity(ResultadoEvaluacion r) {
        var e = new EvaluacionEntity();
        e.cedula = r.cedula();
        e.monto = r.monto();
        e.anios = r.anios();
        e.salario = r.salario();
        e.score = r.score();
        e.deudaMensual = r.deudaMensual();
        e.estado = r.estado();
        e.motivo = r.motivo();
        e.fecha = r.fecha();
        return e;
    }

    private ResultadoEvaluacion aDominio(EvaluacionEntity e) {
        // Reconstruimos la sealed segun el estado guardado.
        // Si el valor en BD no calza con ningun caso conocido, lanzamos para
        // detectar corruption (no degradar silenciosamente).
        return switch (e.estado) {
            case "APROBADO" -> new ResultadoEvaluacion.Aprobado(
                    e.cedula, e.monto, e.anios, e.salario, e.score, e.deudaMensual, e.fecha);
            case "RECHAZADO" -> new ResultadoEvaluacion.Rechazado(
                    e.cedula, e.monto, e.anios, e.salario, e.score, e.deudaMensual, e.fecha,
                    e.motivo == null ? "(sin motivo registrado)" : e.motivo);
            default -> throw new IllegalStateException(
                    "Estado desconocido en BD: " + e.estado + " (id=" + e.id + ")");
        };
    }
}
