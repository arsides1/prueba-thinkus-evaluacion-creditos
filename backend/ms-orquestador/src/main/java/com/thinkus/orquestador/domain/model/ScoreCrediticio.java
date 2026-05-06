package com.thinkus.orquestador.domain.model;

/**
 * Score crediticio devuelto por el buro (Microservicio B).
 *
 * Es un record inmutable con validacion en el compact constructor.
 * Si el buro nos devuelve un valor fuera de rango, la conversion al
 * dominio falla aca y no se propaga ruido al resto de la app.
 */
public record ScoreCrediticio(String cedula, int valor) {

    public ScoreCrediticio {
        if (cedula == null || cedula.isBlank()) {
            throw new IllegalArgumentException("La cedula es obligatoria en ScoreCrediticio");
        }
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException("Score fuera de rango (0-100): " + valor);
        }
    }

    public boolean superaUmbral(int umbral) {
        return valor > umbral;
    }
}
