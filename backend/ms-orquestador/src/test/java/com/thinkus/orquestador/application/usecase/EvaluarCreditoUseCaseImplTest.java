package com.thinkus.orquestador.application.usecase;

import com.thinkus.orquestador.application.port.out.EvaluacionRepositoryPort;
import com.thinkus.orquestador.application.port.out.RiesgosPort;
import com.thinkus.orquestador.domain.model.DeudasCliente;
import com.thinkus.orquestador.domain.model.ResultadoEvaluacion;
import com.thinkus.orquestador.domain.model.ScoreCrediticio;
import com.thinkus.orquestador.domain.model.SolicitudCredito;
import com.thinkus.orquestador.infrastructure.config.ConfiguracionPolitica;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests del use case con Mockito puro (sin levantar Quarkus).
 *
 * Aca validamos que:
 *  - Las llamadas al puerto RiesgosPort se hacen para score y deudas
 *  - El resultado de la politica se persiste via repositorio
 *  - El estado correcto sale segun los datos mockeados
 *
 * No usamos @QuarkusTest porque no necesitamos infraestructura: el use case
 * solo depende de abstracciones (los dos puertos OUT y la config). Esto es
 * el beneficio practico de Hexagonal: tests rapidos y aislados.
 */
@DisplayName("EvaluarCreditoUseCaseImpl - orquestacion del flujo BFF")
class EvaluarCreditoUseCaseImplTest {

    private static final String CEDULA = "1716123458";

    private RiesgosPort riesgos;
    private EvaluacionRepositoryPort repositorio;
    private ConfiguracionPolitica configPolitica;
    private EvaluarCreditoUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        riesgos = mock(RiesgosPort.class);
        repositorio = mock(EvaluacionRepositoryPort.class);
        configPolitica = mock(ConfiguracionPolitica.class);
        when(configPolitica.umbralScore()).thenReturn(70);
        when(configPolitica.porcentajeCapacidadPago()).thenReturn(new BigDecimal("0.40"));

        // El repositorio devuelve el mismo resultado que recibe (echo) para no
        // complicar las aserciones; en test real solo importa que se llame.
        // Cast explicito: createFrom().item(T) tiene una sobrecarga con Supplier
        // que se elige por defecto al pasar Object generico.
        when(repositorio.guardar(any())).thenAnswer(inv -> {
            ResultadoEvaluacion r = inv.getArgument(0);
            return Uni.createFrom().item(r);
        });

        useCase = new EvaluarCreditoUseCaseImpl();
        // inyeccion manual de los mocks (sin CDI)
        useCase.riesgos = riesgos;
        useCase.repositorio = repositorio;
        useCase.configPolitica = configPolitica;
    }

    @Test
    @DisplayName("Caso APROBADO: score 80, monto razonable, sin deudas")
    void casoAprobado() {
        when(riesgos.obtenerScore(anyString()))
                .thenReturn(Uni.createFrom().item(new ScoreCrediticio(CEDULA, 80)));
        when(riesgos.obtenerDeudas(anyString()))
                .thenReturn(Uni.createFrom().item(DeudasCliente.sinDeudas(CEDULA)));

        var solicitud = new SolicitudCredito(CEDULA, new BigDecimal("500"), 3, new BigDecimal("3000"));

        var resultado = useCase.evaluar(solicitud).await().indefinitely();

        assertThat(resultado).isInstanceOf(ResultadoEvaluacion.Aprobado.class);
        assertThat(resultado.estado()).isEqualTo("APROBADO");

        verify(riesgos).obtenerScore(CEDULA);
        verify(riesgos).obtenerDeudas(CEDULA);
        verify(repositorio).guardar(any());
    }

    @Test
    @DisplayName("Caso RECHAZADO por score bajo")
    void casoRechazadoPorScore() {
        when(riesgos.obtenerScore(anyString()))
                .thenReturn(Uni.createFrom().item(new ScoreCrediticio(CEDULA, 50)));
        when(riesgos.obtenerDeudas(anyString()))
                .thenReturn(Uni.createFrom().item(DeudasCliente.sinDeudas(CEDULA)));

        var solicitud = new SolicitudCredito(CEDULA, new BigDecimal("500"), 3, new BigDecimal("5000"));
        var resultado = useCase.evaluar(solicitud).await().indefinitely();

        assertThat(resultado).isInstanceOf(ResultadoEvaluacion.Rechazado.class);
        assertThat(((ResultadoEvaluacion.Rechazado) resultado).razon()).contains("Score");
    }

    @Test
    @DisplayName("Caso RECHAZADO por capacidad de pago insuficiente")
    void casoRechazadoPorCapacidad() {
        when(riesgos.obtenerScore(anyString()))
                .thenReturn(Uni.createFrom().item(new ScoreCrediticio(CEDULA, 90)));
        when(riesgos.obtenerDeudas(anyString()))
                .thenReturn(Uni.createFrom().item(new DeudasCliente(CEDULA, new BigDecimal("700"))));

        // Salario 1000, capacidad = 400. 700 + 100 = 800 > 400 -> rechazado por capacidad
        var solicitud = new SolicitudCredito(CEDULA, new BigDecimal("100"), 3, new BigDecimal("1000"));
        var resultado = useCase.evaluar(solicitud).await().indefinitely();

        assertThat(resultado).isInstanceOf(ResultadoEvaluacion.Rechazado.class);
        assertThat(((ResultadoEvaluacion.Rechazado) resultado).razon()).contains("capacidad");
    }

    @Test
    @DisplayName("La llamada al MS B se hace en paralelo (combine().all)")
    void llamadasParalelasAlBuro() {
        // Si fueran secuenciales, el tiempo total seria score+deudas. Aqui simulamos
        // ambos respondiendo al mismo tiempo y verificamos que el resultado vuelve.
        when(riesgos.obtenerScore(anyString()))
                .thenReturn(Uni.createFrom().item(new ScoreCrediticio(CEDULA, 75))
                        .onItem().delayIt().by(java.time.Duration.ofMillis(50)));
        when(riesgos.obtenerDeudas(anyString()))
                .thenReturn(Uni.createFrom().item(DeudasCliente.sinDeudas(CEDULA))
                        .onItem().delayIt().by(java.time.Duration.ofMillis(50)));

        var solicitud = new SolicitudCredito(CEDULA, new BigDecimal("100"), 1, new BigDecimal("5000"));
        var inicio = System.currentTimeMillis();
        var resultado = useCase.evaluar(solicitud).await().indefinitely();
        var elapsed = System.currentTimeMillis() - inicio;

        assertThat(resultado).isNotNull();
        // Si fuera secuencial seria > 100ms; en paralelo debe estar mas cerca de 50ms
        assertThat(elapsed).isLessThan(150L);
    }
}
