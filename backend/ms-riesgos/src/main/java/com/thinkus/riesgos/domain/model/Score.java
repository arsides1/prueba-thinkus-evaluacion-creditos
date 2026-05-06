package com.thinkus.riesgos.domain.model;

/**
 * Puntaje crediticio del solicitante.
 *
 * Valor entero entre 0 y 100. El compact constructor valida la
 * invariante porque sin ella el dominio se llena de objetos invalidos
 * que despues hay que defender en cada capa.
 */
public record Score(String cedula, int valor) {

    public Score {
        // si llega null o fuera de rango lo cortamos aca, no en el resource
        if (cedula == null || cedula.isBlank()) {
            throw new IllegalArgumentException("La cedula es obligatoria");
        }
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException("Score fuera de rango (0-100): " + valor);
        }
    }
}
