package com.thinkus.orquestador.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA mapeada a la tabla 'evaluaciones'.
 *
 * Esta clase NO es el modelo de dominio. Es la representacion de la fila
 * en BD, anotada con JPA. El dominio (ResultadoEvaluacion sealed interface)
 * se mapea a esta entidad solo en este adapter; el resto de la app no se
 * entera de que existe Hibernate.
 *
 * Se usa class y no record porque Hibernate todavia no soporta records como
 * @Entity de forma fluida (necesita constructor vacio y setters).
 */
@Entity
@Table(name = "evaluaciones")
public class EvaluacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "cedula", nullable = false, length = 10)
    public String cedula;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    public BigDecimal monto;

    @Column(name = "anios", nullable = false)
    public int anios;

    @Column(name = "salario", nullable = false, precision = 15, scale = 2)
    public BigDecimal salario;

    @Column(name = "score", nullable = false)
    public int score;

    @Column(name = "deuda_mensual", nullable = false, precision = 15, scale = 2)
    public BigDecimal deudaMensual;

    @Column(name = "estado", nullable = false, length = 20)
    public String estado;

    @Column(name = "motivo", length = 255)
    public String motivo;

    @Column(name = "fecha", nullable = false)
    public LocalDateTime fecha;

    public EvaluacionEntity() {
        // requerido por Hibernate
    }
}
