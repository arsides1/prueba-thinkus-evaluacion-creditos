package com.thinkus.riesgos.infrastructure.adapter.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Smoke tests del recurso de riesgos.
 *
 * Aca confirmamos que los endpoints estan vivos, devuelven el formato
 * esperado y aplican validacion. La latencia esta bajada a 10ms en %test
 * (ver application.properties) para que la suite no demore.
 *
 * Cedulas usadas: las mismas validas del CedulaECValidatorTest del modulo
 * commons, asi mantenemos un set conocido y trazable.
 */
@QuarkusTest
@DisplayName("RecursoRiesgos - smoke tests")
class RecursoRiesgosTest {

    private static final String CEDULA_VALIDA = "1716123458";
    private static final String CEDULA_INVALIDA = "1234567890"; // digito verificador erroneo

    @Test
    @DisplayName("GET /v1/riesgos/score/{cedula} devuelve 200 con score 0..100")
    void scoreFelizPath() {
        given()
                .when().get("/v1/riesgos/score/" + CEDULA_VALIDA)
                .then()
                .statusCode(200)
                .body("cedula", equalTo(CEDULA_VALIDA))
                .body("score", allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(100)));
    }

    @Test
    @DisplayName("GET /v1/riesgos/score con cedula invalida devuelve 400")
    void scoreCedulaInvalida() {
        given()
                .when().get("/v1/riesgos/score/" + CEDULA_INVALIDA)
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("title", notNullValue());
    }

    @Test
    @DisplayName("GET /v1/riesgos/deudas/{cedula} devuelve 200 con lista y total")
    void deudasFelizPath() {
        given()
                .when().get("/v1/riesgos/deudas/" + CEDULA_VALIDA)
                .then()
                .statusCode(200)
                .body("cedula", equalTo(CEDULA_VALIDA))
                .body("deudas", notNullValue())
                // entre 0 y 5 deudas (segun config)
                .body("deudas.size()", allOf(greaterThanOrEqualTo(0), lessThanOrEqualTo(5)))
                .body("mensualidadTotal", notNullValue());
    }

    @Test
    @DisplayName("GET /v1/riesgos/deudas con cedula invalida devuelve 400")
    void deudasCedulaInvalida() {
        given()
                .when().get("/v1/riesgos/deudas/" + CEDULA_INVALIDA)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Health check liveness responde 200")
    void healthLiveness() {
        given()
                .when().get("/q/health/live")
                .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    @DisplayName("OpenAPI spec esta expuesto en /q/openapi")
    void openApiExpuesto() {
        given()
                .when().get("/q/openapi")
                .then()
                .statusCode(200);
    }
}
