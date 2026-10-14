import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { LibellePipe } from '../core/libelle.pipe';

@Component({
  selector: 'app-shell', standalone: true, imports: [RouterOutlet, RouterLink, RouterLinkActive, LibellePipe],
  template: `
  <aside>
    <div class="logo">STB<span>Réclamations</span></div>
    <nav>
      <a routerLink="/tableau-de-bord" routerLinkActive="actif">Tableau de bord</a>
      <a routerLink="/reclamations" routerLinkActive="actif" [routerLinkActiveOptions]="{exact: true}">Réclamations</a>
      <a routerLink="/reclamations/nouvelle" routerLinkActive="actif">Nouvelle réclamation</a>
    </nav>
    <div class="profil">
      <strong>{{ auth.session()?.nomComplet }}</strong>
      <span>{{ auth.session()?.role }}</span>
      <button class="btn secondaire" (click)="auth.logout()">Déconnexion</button>
    </div>
  </aside>
  <main><router-outlet /></main>`,
  styles: [`
    :host { display: flex; min-height: 100vh; }
    aside { width: 230px; background: var(--bleu); color: #fff; display: flex; flex-direction: column; padding: 22px 14px; flex-shrink: 0; }
    .logo { font-weight: 800; font-size: 22px; margin: 0 8px 28px; } .logo span { font-weight: 400; font-size: 14px; margin-left: 5px; opacity: .8; }
    nav { display: flex; flex-direction: column; gap: 4px; flex: 1; }
    nav a { color: #dbe6f5; text-decoration: none; padding: 10px 12px; border-radius: 8px; }
    nav a.actif, nav a:hover { background: rgba(255,255,255,.14); color: #fff; }
    .profil { display: flex; flex-direction: column; gap: 4px; padding: 8px; font-size: 13px; }
    .profil span { opacity: .7; margin-bottom: 8px; }
    main { flex: 1; padding: 28px 32px; min-width: 0; }
  `],
})
export class ShellComponent { auth = inject(AuthService); }
