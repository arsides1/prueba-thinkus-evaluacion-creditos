package com.thinkus.commons.exception;

/**
 * Excepción base para errores de reglas de negocio del dominio.
 *
 * <p>Esta clase representa errores esperados y recuperables, distintos de
 * fallos técnicos. Los {@code GlobalExceptionMapper} en cada microservicio
 * la traducen a códigos HTTP semánticamente correctos (típicamente 400 o 422).
 */
public class BusinessException extends RuntimeException {

    private final String codigo;

    public BusinessException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public BusinessException(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
