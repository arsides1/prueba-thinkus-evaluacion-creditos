-- =========================================================================
-- Migracion V1: tabla de evaluaciones crediticias
-- =========================================================================
-- Cada vez que el orquestador procesa una solicitud, persiste un registro
-- aqui. El reviewer puede consultar la tabla para ver el historial.
--
-- Estado se restringe con CHECK porque el dominio solo acepta dos valores.
-- Es mas barato que un enum y deja la validacion en la BD donde tambien aplica.
--
-- Indices:
--   - cedula: para listar evaluaciones por cliente
--   - fecha: para ordenar por mas recientes (uso comun en UI)
-- =========================================================================

CREATE TABLE IF NOT EXISTS evaluaciones (
    id              BIGSERIAL       PRIMARY KEY,
    cedula          VARCHAR(10)     NOT NULL,
    monto           NUMERIC(15, 2)  NOT NULL CHECK (monto > 0),
    anios           INTEGER         NOT NULL CHECK (anios > 0),
    salario         NUMERIC(15, 2)  NOT NULL CHECK (salario > 0),
    score           INTEGER         NOT NULL CHECK (score BETWEEN 0 AND 100),
    deuda_mensual   NUMERIC(15, 2)  NOT NULL DEFAULT 0,
    estado          VARCHAR(20)     NOT NULL CHECK (estado IN ('APROBADO', 'RECHAZADO')),
    motivo          VARCHAR(255),
    fecha           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_evaluaciones_cedula ON evaluaciones (cedula);
CREATE INDEX IF NOT EXISTS idx_evaluaciones_fecha  ON evaluaciones (fecha DESC);

COMMENT ON TABLE evaluaciones IS 'Historial de evaluaciones crediticias procesadas por el orquestador';
COMMENT ON COLUMN evaluaciones.estado IS 'APROBADO o RECHAZADO segun la politica crediticia aplicada';
COMMENT ON COLUMN evaluaciones.motivo IS 'Detalle textual del motivo, util cuando el resultado es RECHAZADO';
