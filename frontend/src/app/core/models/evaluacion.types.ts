/**
 * Tipos compartidos del feature de evaluacion crediticia.
 *
 * Se mantienen alineados con los DTOs del modulo commons del backend.
 * En un proyecto mas grande se podria generar TS types automaticamente
 * desde el OpenAPI con openapi-typescript-generator, pero para 4 records
 * tener los types a mano es mas simple y suficiente.
 */

/** Payload del POST /v1/credit-evaluations */
export interface EvaluacionRequest {
  cedula: string;
  monto: number;
  anios: number;
  salario: number;
}

/** Respuesta del POST y de cada item del listado */
export interface EvaluacionResponse {
  id: number | null;
  cedula: string;
  monto: number;
  estado: 'APROBADO' | 'RECHAZADO';
  motivo: string;
  score: number;
  fecha: string; // ISO desde el backend, lo formateamos en el componente
}

/** Estructura comun para errores Problem Details (RFC 7807) */
export interface ProblemDetails {
  status: number;
  title: string;
  detail?: string;
  instant?: string;
  errores?: Array<{ campo: string; valor: string; mensaje: string }>;
}
