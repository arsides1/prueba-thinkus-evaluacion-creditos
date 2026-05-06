package com.thinkus.riesgos.domain.model;

import java.math.BigDecimal;

/**
 * Una deuda del solicitante con la cuota mensual asociada.
 *
 * Se usa BigDecimal y no double porque estamos hablando de plata; no quiero
 * sorpresas de redondeo al sumar mensualidades.
 */
public record Deuda(String descripcion, BigDecimal mensualidad) {

    public Deuda {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripcion de la deuda es obligatoria");
        }
        if (mensualidad == null || mensualidad.signum() <= 0) {
            throw new IllegalArgumentException("La mensualidad debe ser positiva");
        }
    }
}
