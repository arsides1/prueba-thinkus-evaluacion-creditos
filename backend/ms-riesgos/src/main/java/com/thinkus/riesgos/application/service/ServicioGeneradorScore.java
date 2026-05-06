package com.thinkus.riesgos.application.service;

import com.thinkus.riesgos.domain.model.Score;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Genera un score crediticio aleatorio para una cedula.
 *
 * Mock: devuelve un entero uniforme entre 0 y 100. En un sistema real este
 * componente seria un cliente al buro de credito (Equifax, Experian, etc.).
 *
 * Stateless y thread-safe: ThreadLocalRandom es la opcion correcta cuando
 * se llama desde multiples hilos sin contension. java.util.Random es
 * sincronizado y se vuelve cuello de botella bajo carga.
 */
@ApplicationScoped
public class ServicioGeneradorScore {

    public Score generar(String cedula) {
        int valor = ThreadLocalRandom.current().nextInt(0, 101); // 0..100 inclusive
        return new Score(cedula, valor);
    }
}
