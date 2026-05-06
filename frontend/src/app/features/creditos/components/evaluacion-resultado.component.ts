import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';

import { EvaluacionResponse } from '../../../core/models/evaluacion.types';

/**
 * Tarjeta que muestra el resultado de una evaluacion (APROBADO / RECHAZADO).
 *
 * Componente puro de presentacion: recibe el resultado por input() y lo
 * pinta. Sin estado interno propio, sin servicios.
 *
 * Usa input.required() de Angular 17+: si la prop falta, falla en build,
 * no en runtime. Mucho mas seguro que los @Input() de antes.
 */
@Component({
  selector: 'app-evaluacion-resultado',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './evaluacion-resultado.component.html',
  styleUrl: './evaluacion-resultado.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EvaluacionResultadoComponent {
  readonly data = input.required<EvaluacionResponse>();

  // computed se recalcula automaticamente cuando cambia data().
  // Util para no repetir la condicional en el template.
  readonly esAprobado = computed(() => this.data().estado === 'APROBADO');
}
