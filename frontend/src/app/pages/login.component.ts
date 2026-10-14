import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-login', standalone: true, imports: [FormsModule, RouterLink],
  template: `
  <div class="page">
    <div class="panneau">
      <div class="logo">STB<span>Réclamations</span></div>
      <h1>Espace collaborateurs</h1>
      <p class="sous-titre">Connectez-vous pour traiter les réclamations clients.</p>
      <form (ngSubmit)="connexion()">
        <div class="champ"><label>E-mail professionnel</label><input name="email" [(ngModel)]="email" type="email" required></div>
        <div class="champ"><label>Mot de passe</label><input name="mdp" [(ngModel)]="motDePasse" type="password" required></div>
        <button class="btn" style="width:100%" [disabled]="chargement">Se connecter</button>
        @if (erreur) { <p class="erreur">{{ erreur }}</p> }
      </form>
      <p class="lien">Vous êtes client ? <a routerLink="/portail">Déposer ou suivre une réclamation</a></p>
    </div>
  </div>`,
  styles: [`
    .page { min-height: 100vh; display: grid; place-items: center; background: linear-gradient(135deg, #0b3d7a, #0f8a5f); padding: 16px; }
    .panneau { background: #fff; border-radius: 14px; padding: 36px; width: 100%; max-width: 400px; box-shadow: 0 20px 50px rgba(0,0,0,.2); }
    .logo { font-weight: 800; font-size: 26px; color: var(--bleu); margin-bottom: 22px; }
    .logo span { font-weight: 400; color: var(--vert); margin-left: 6px; font-size: 18px; }
    .lien { margin-top: 18px; font-size: 13px; color: var(--gris); } a { color: var(--bleu); }
  `],
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  email = ''; motDePasse = ''; erreur = ''; chargement = false;

  connexion() {
    this.chargement = true; this.erreur = '';
    this.auth.login(this.email, this.motDePasse).subscribe({
      next: () => this.router.navigate(['/tableau-de-bord']),
      error: () => { this.erreur = 'E-mail ou mot de passe incorrect'; this.chargement = false; },
    });
  }
}
