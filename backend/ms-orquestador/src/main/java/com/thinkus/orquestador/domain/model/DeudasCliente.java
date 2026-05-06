package com.thinkus.orquestador.domain.model;

import java.math.BigDecimal;

/**
 * Resumen de deudas del cliente devuelto por el MS B.
 *
 * No detallamos cada deuda individual en el dominio del orquestador
 * porque para aplicar la politica solo importa la suma total. Esto
 * mantiene el dominio enfocado en lo que decide.
 */
public record DeudasCliente(String cedula, BigDecimal mensualidadTotal) {

    public DeudasCliente {
        if (cedula == null || cedula.isBlank()) {
            throw new IllegalArgumentException("La cedula es obligatoria");
        }
        if (mensualidadTotal == null || mensualidadTotal.signum() < 0) {
            throw new IllegalArgumentException("La mensualidad total no puede ser negativa: " + mensualidadTotal);
        }
    }

    public static DeudasCliente sinDeudas(String cedula) {
        return new DeudasCliente(cedula, BigDecimal.ZERO);
    }
}
