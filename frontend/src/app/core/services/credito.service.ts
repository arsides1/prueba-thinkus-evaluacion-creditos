import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { EvaluacionRequest, EvaluacionResponse } from '../models/evaluacion.types';

/**
 * Servicio de comunicacion con el orquestador (Microservicio A).
 *
 * Se inyecta con inject() en lugar de constructor: es la forma moderna
 * recomendada por Angular 18+, mas concisa y permite usarlo en funciones
 * (guards, resolvers, interceptors) ademas de en clases.
 *
 * Devuelve Observables porque HttpClient los devuelve nativamente. Los
 * componentes consumidores los convierten a signals con toSignal() o se
 * suscriben directo.
 */
@Injectable({ providedIn: 'root' })
export class CreditoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBase}/v1/credit-evaluations`;

  evaluar(payload: EvaluacionRequest): Observable<EvaluacionResponse> {
    return this.http.post<EvaluacionResponse>(this.base, payload);
  }

  listar(): Observable<EvaluacionResponse[]> {
    return this.http.get<EvaluacionResponse[]>(this.base);
  }
}
