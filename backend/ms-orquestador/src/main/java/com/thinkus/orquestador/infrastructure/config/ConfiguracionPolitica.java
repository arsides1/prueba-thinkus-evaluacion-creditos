package com.thinkus.orquestador.infrastructure.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.math.BigDecimal;

/**
 * Configuracion tipada de la politica crediticia.
 *
 * Tener estos valores en config (en vez de hardcodeados) permite ajustar
 * la politica por ambiente (mas restrictiva en dev, mas laxa en pruebas)
 * sin tocar el codigo de dominio.
 *
 * Vive en infrastructure porque @ConfigMapping es de Quarkus (un detalle
 * de framework). El dominio recibe los valores ya como tipos primitivos.
 */
@ConfigMapping(prefix = "app.politica")
public interface ConfiguracionPolitica {

    /** Score minimo (exclusivo) que debe tener el solicitante. Default 70. */
    @WithDefault("70")
    int umbralScore();

    /** Porcentaje del salario disponible para nueva deuda. Default 0.40 (40%). */
    @WithDefault("0.40")
    BigDecimal porcentajeCapacidadPago();
}
