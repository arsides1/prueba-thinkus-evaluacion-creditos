import { ChangeDetectionStrategy, Component, inject, output } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { EvaluacionRequest } from '../../../core/models/evaluacion.types';
import { cedulaEcuatorianaValidator } from '../../../shared/validators/cedula-ec.validator';

/**
 * Formulario de solicitud de evaluacion crediticia.
 *
 * Componente "dumb": no inyecta servicios, no llama HTTP, solo emite el
 * evento `submitted` con el payload listo. La page lo escucha y lo manda
 * al servicio. Esto facilita testear el form aislado.
 *
 * Reactive Forms con NonNullableFormBuilder: evita lidiar con `value | null`
 * en cada control. En la version no-null, getRawValue() devuelve un objeto
 * fuertemente tipado sin `| null` en los campos.
 */
@Component({
  selector: 'app-evaluacion-form',
  imports: [ReactiveFormsModule],
  templateUrl: './evaluacion-form.component.html',
  styleUrl: './evaluacion-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EvaluacionFormComponent {
  protected readonly fb = inject(NonNullableFormBuilder);

  // Output moderno (Angular 17.3+): mas conciso que @Output() EventEmitter
  readonly submitted = output<EvaluacionRequest>();

  protected readonly form = this.fb.group({
    cedula: this.fb.control('', [Validators.required, cedulaEcuatorianaValidator()]),
    monto: this.fb.control(0, [Validators.required, Validators.min(1)]),
    anios: this.fb.control(1, [Validators.required, Validators.min(1), Validators.max(30)]),
    salario: this.fb.control(0, [Validators.required, Validators.min(1)]),
  });

  // Getters para bindear errores en el template sin meter logica en el HTML
  protected get cedulaCtrl() {
    return this.form.controls.cedula;
  }
  protected get montoCtrl() {
    return this.form.controls.monto;
  }
  protected get aniosCtrl() {
    return this.form.controls.anios;
  }
  protected get salarioCtrl() {
    return this.form.controls.salario;
  }

  protected onSubmit(): void {
    if (this.form.invalid) {
      // markAllAsTouched fuerza que se muestren los mensajes de error
      // aunque el usuario no haya tocado los campos. UX standard.
      this.form.markAllAsTouched();
      return;
    }
    this.submitted.emit(this.form.getRawValue());
  }
}
