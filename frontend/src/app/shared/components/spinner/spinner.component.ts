import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Spinner de carga estilo institucional. Aparece mientras el orquestador
 * consulta el buro y aplica la politica.
 */
@Component({
  selector: 'app-spinner',
  template: `
    <div class="tk-loading">
      <div class="tk-spinner"></div>
      @if (mensaje()) {
        <div class="tk-loading-text">{{ mensaje() }}</div>
      }
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpinnerComponent {
  readonly mensaje = input<string>('');
}
