package com.thinkus.riesgos.application.service;

import com.thinkus.riesgos.domain.model.Deuda;
import com.thinkus.riesgos.infrastructure.config.ConfiguracionApp;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Genera una lista aleatoria de deudas para una cedula.
 *
 * El rango de cantidad y los topes de mensualidad vienen de la configuracion
 * para poder ajustarlos por ambiente sin recompilar (en %test bajamos los
 * limites, por ejemplo).
 *
 * Algunas cedulas no tendran deudas: el "min" puede ser 0. Eso es a proposito
 * para que el orquestador pruebe ambos caminos (lista vacia vs con deudas).
 */
@ApplicationScoped
public class ServicioGeneradorDeudas {

    private static final List<String> CATALOGO_DESCRIPCIONES = List.of(
            "Tarjeta de credito Banco Pichincha",
            "Credito de consumo Banco Guayaquil",
            "Prestamo automotriz Produbanco",
            "Credito hipotecario BIESS",
            "Tarjeta departamental Comandato",
            "Credito educativo IECE",
            "Credito microempresa CFN"
    );

    @Inject
    ConfiguracionApp config;

    public List<Deuda> generar(String cedula) {
        var rnd = ThreadLocalRandom.current();
        int cantidad = rnd.nextInt(config.deudas().min(), config.deudas().max() + 1);

        if (cantidad == 0) {
            return List.of(); // sin deudas: lista inmutable vacia
        }

        var resultado = new ArrayList<Deuda>(cantidad);
        for (int i = 0; i < cantidad; i++) {
            String descripcion = CATALOGO_DESCRIPCIONES.get(
                    rnd.nextInt(CATALOGO_DESCRIPCIONES.size()));

            // Centavos: generamos un entero y dividimos para evitar doubles raros
            int centavos = rnd.nextInt(
                    config.deudas().mensualidadMin() * 100,
                    config.deudas().mensualidadMax() * 100 + 1);
            BigDecimal mensualidad = BigDecimal.valueOf(centavos)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            resultado.add(new Deuda(descripcion, mensualidad));
        }
        return resultado;
    }

    /**
     * Suma las mensualidades. Esto se calcula aca y no en el cliente para
     * evitar que cada consumidor implemente la suma con su propio redondeo.
     */
    public BigDecimal sumarMensualidades(List<Deuda> deudas) {
        return deudas.stream()
                .map(Deuda::mensualidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
