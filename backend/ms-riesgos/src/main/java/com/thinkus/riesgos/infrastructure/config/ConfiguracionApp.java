package com.thinkus.riesgos.infrastructure.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuracion tipada del servicio. Reemplaza el clasico @ConfigProperty
 * disperso por todas las clases. Mucho mas claro tener todo agrupado y
 * con valores por defecto explicitos.
 *
 * Las propiedades viven en application.properties bajo el prefijo "app.".
 */
@ConfigMapping(prefix = "app")
public interface ConfiguracionApp {

    Latencia latencia();

    Deudas deudas();

    interface Latencia {
        /** Milisegundos a dormir antes de devolver el score. Default 2 segundos como pide el enunciado. */
        @WithDefault("2000")
        long scoreMs();

        /** Milisegundos a dormir antes de devolver las deudas. Default 1.5 segundos. */
        @WithDefault("1500")
        long deudasMs();
    }

    interface Deudas {
        /** Cantidad minima de deudas a generar (puede ser 0, no todos tienen deudas). */
        @WithDefault("0")
        int min();

        /** Cantidad maxima de deudas a generar. */
        @WithDefault("5")
        int max();

        /** Mensualidad minima en dolares. */
        @WithDefault("50")
        int mensualidadMin();

        /** Mensualidad maxima en dolares. */
        @WithDefault("800")
        int mensualidadMax();
    }
}
