import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';

import { routes } from './app.routes';

/**
 * Configuracion raiz de la aplicacion.
 *
 * - provideHttpClient(withFetch()) usa el fetch nativo del navegador en lugar
 *   de XHR. Mejor en SSR (no aplica aqui) y mas moderno.
 * - withComponentInputBinding() permite que parametros de la ruta se mapeen
 *   directo a inputs del componente con input.required<string>().
 * - provideZoneChangeDetection con eventCoalescing reduce ciclos de CD
 *   agrupando eventos del mismo tick. Un default razonable.
 *
 * NO usamos provideZonelessChangeDetection todavia porque algunas librerias
 * (Bootstrap JS, por ejemplo) todavia dependen de Zone.js para detectar
 * cambios. Cuando todo el ecosistema este listo, se migra.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withFetch()),
  ],
};
