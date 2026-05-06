package com.thinkus.orquestador.infrastructure.adapter.out.persistence;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repositorio Panache reactivo para la entidad EvaluacionEntity.
 *
 * Por que Repository y no Active Record (la entidad extendiendo
 * PanacheEntity): el Repository pattern separa entidad de acceso a
 * datos, facilita mockear en tests y mantiene la entidad como POJO de
 * persistencia, sin metodos de negocio.
 *
 * Esta clase expone TODOS los metodos heredados de PanacheRepository
 * (persist, findById, listAll, count, delete, etc.) sin necesidad de
 * declararlos. Si manana necesitamos un finder custom, se anade aqui.
 */
@ApplicationScoped
public class RepositorioPanacheEvaluaciones implements PanacheRepository<EvaluacionEntity> {
}
