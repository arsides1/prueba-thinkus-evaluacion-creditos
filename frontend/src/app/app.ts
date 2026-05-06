import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/**
 * Shell de la aplicacion: navbar + router-outlet + footer institucional.
 *
 * Solo orquesta layout; cada vista se carga desde app.routes.ts.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly buildVersion = '1.0.0';
}
