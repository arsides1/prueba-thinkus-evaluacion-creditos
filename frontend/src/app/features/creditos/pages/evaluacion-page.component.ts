import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CreditoService } from '../../../core/services/credito.service';
import {
  EvaluacionRequest,
  EvaluacionResponse,
  ProblemDetails,
} from '../../../core/models/evaluacion.types';
import { EvaluacionFormComponent } from '../components/evaluacion-form.component';
import { EvaluacionResultadoComponent } from '../components/evaluacion-resultado.component';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';

/**
 * Pagina principal: form + resultado + manejo de loading/error.
 *
 * "Smart component": inyecta el servicio, mantiene el estado de la pantalla
 * (loading, error, resultado) con signals, y delega la presentacion a los
 * componentes hijos (form, resultado, spinner).
 *
 * Uso de signals en lugar de | async pipe: el codigo queda imperativo pero
 * legible y se evita el riesgo de multiples subscripciones cuando el
 * mismo Observable se referencia varias veces en el template.
 */
@Component({
  selector: 'app-evaluacion-page',
  imports: [
    EvaluacionFormComponent,
    EvaluacionResultadoComponent,
    SpinnerComponent,
    RouterLink,
  ],
  templateUrl: './evaluacion-page.component.html',
  styleUrl: './evaluacion-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EvaluacionPageComponent {
  private readonly creditoService = inject(CreditoService);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly resultado = signal<EvaluacionResponse | null>(null);

  protected onSubmit(payload: EvaluacionRequest): void {
    this.loading.set(true);
    this.error.set(null);
    this.resultado.set(null);

    this.creditoService.evaluar(payload).subscribe({
      next: (r) => {
        this.resultado.set(r);
        this.loading.set(false);
      },
      error: (httpErr) => {
        this.error.set(this.extraerMensaje(httpErr));
        this.loading.set(false);
      },
    });
  }

  /**
   * Extrae el mensaje legible de un error HTTP.
   *
   * El backend devuelve Problem Details (RFC 7807); cuando es 400 con
   * detalle por campo armamos un mensaje bonito; en otros casos usamos el
   * detail o un fallback.
   */
  private extraerMensaje(httpErr: { error?: ProblemDetails | string; status?: number }): string {
    const body = httpErr?.error;
    if (typeof body === 'string') return body;
    if (!body) return 'Error de comunicación con el servicio.';

    const problema = body as ProblemDetails;
    if (problema.errores?.length) {
      return problema.errores.map((e) => `${e.campo}: ${e.mensaje}`).join(' • ');
    }
    return problema.detail ?? problema.title ?? 'Error desconocido';
  }
}
