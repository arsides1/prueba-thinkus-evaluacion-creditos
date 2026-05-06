package com.thinkus.orquestador.domain.exception;

/**
 * Lanzada cuando la solicitud o sus datos asociados no permiten evaluar
 * (por ejemplo, cuando el buro no responde y no podemos aplicar la politica).
 *
 * Es runtime para no contaminar firmas. La capa REST la mapea a 422.
 */
public class EvaluacionInvalidaException extends RuntimeException {

    public EvaluacionInvalidaException(String mensaje) {
        super(mensaje);
    }

    public EvaluacionInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
