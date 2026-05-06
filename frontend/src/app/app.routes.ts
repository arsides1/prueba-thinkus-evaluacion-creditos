import { Routes } from '@angular/router';
import { EvaluacionPageComponent } from './features/creditos/pages/evaluacion-page.component';
import { HistorialPageComponent } from './features/creditos/pages/historial-page.component';

/**
 * Rutas de la aplicacion.
 *
 * Se importan los componentes directamente (no lazy) porque la app es
 * pequenia y el bundle resulta liviano. Si crece, se migra a
 * loadComponent por ruta sin tocar el resto.
 */
export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: EvaluacionPageComponent,
    title: 'Evaluación de crédito',
  },
  {
    path: 'historial',
    component: HistorialPageComponent,
    title: 'Historial de evaluaciones',
  },
  {
    path: '**',
    redirectTo: '',
  },
];
