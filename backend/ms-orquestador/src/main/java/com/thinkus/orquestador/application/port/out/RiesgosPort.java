package com.thinkus.orquestador.application.port.out;

import com.thinkus.orquestador.domain.model.DeudasCliente;
import com.thinkus.orquestador.domain.model.ScoreCrediticio;
import io.smallrye.mutiny.Uni;

/**
 * Puerto OUT para consultar el buro de riesgos.
 *
 * El use case depende de esta abstraccion, NO del cliente REST concreto.
 * Esto es Dependency Inversion (DIP) en accion: si manana migramos a gRPC
 * o a un buro distinto, solo cambia el adapter, el dominio no se entera.
 *
 * Devuelve Uni para mantener el stack reactivo end-to-end. La paralelizacion
 * de las dos llamadas se hace en el use case con Uni.combine().all().
 */
public interface RiesgosPort {

    Uni<ScoreCrediticio> obtenerScore(String cedula);

    Uni<DeudasCliente> obtenerDeudas(String cedula);
}
