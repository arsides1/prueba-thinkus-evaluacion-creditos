import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { CreditoService } from '../../../core/services/credito.service';
import { EvaluacionResponse } from '../../../core/models/evaluacion.types';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';

/**
 * Pagina de historial: KPIs agregados + tabla detallada de evaluaciones.
 *
 * Carga al inicializar y mantiene el estado en signals. Incluye un
 * resumen visual con totales que ayuda a interpretar la tabla rapido.
 */
@Component({
  selector: 'app-historial-page',
  imports: [SpinnerComponent, CurrencyPipe, DatePipe, DecimalPipe, RouterLink],
  templateUrl: './historial-page.component.html',
  styleUrl: './historial-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistorialPageComponent implements OnInit {
  private readonly creditoService = inject(CreditoService);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly evaluaciones = signal<EvaluacionResponse[]>([]);

  // KPIs agregados, derivados de la lista. Se recalculan automaticamente
  // gracias a computed cuando cambia la signal evaluaciones().
  protected readonly total = computed(() => this.evaluaciones().length);
  protected readonly aprobadas = computed(
    () => this.evaluaciones().filter((e) => e.estado === 'APROBADO').length,
  );
  protected readonly rechazadas = computed(() => this.total() - this.aprobadas());
  protected readonly tasaAprobacion = computed(() =>
    this.total() === 0 ? 0 : (this.aprobadas() / this.total()) * 100,
  );
  protected readonly montoTotal = computed(() =>
    this.evaluaciones().reduce((sum, e) => sum + e.monto, 0),
  );

  ngOnInit(): void {
    this.recargar();
  }

  protected recargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.creditoService.listar().subscribe({
      next: (lista) => {
        this.evaluaciones.set(lista);
        this.loading.set(false);
      },
      error: (httpErr) => {
        this.error.set(httpErr?.error?.detail ?? 'No se pudo cargar el historial.');
        this.loading.set(false);
      },
    });
  }
}
