import { Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ReclamationService } from '../core/reclamation.service';
import { SuiviPublic } from '../core/models';
import { LibellePipe } from '../core/libelle.pipe';

@Component({
  selector: 'app-portail', standalone: true, imports: [FormsModule, DatePipe, RouterLink, LibellePipe],
  template: `
  <header><div class="logo">STB<span>Espace réclamations</span></div><a routerLink="/login">Espace collaborateurs</a></header>
  <div class="contenu">
    <div class="onglets">
      <button [class.actif]="onglet==='depot'" (click)="onglet='depot'">Déposer une réclamation</button>
      <button [class.actif]="onglet==='suivi'" (click)="onglet='suivi'">Suivre ma réclamation</button>
    </div>
    @if (onglet === 'depot') {
      <form class="carte" (ngSubmit)="deposer()">
        <div class="ligne">
          <div class="champ"><label>Nom complet</label><input name="n" [(ngModel)]="f.nomClient" required></div>
          <div class="champ"><label>E-mail</label><input name="e" [(ngModel)]="f.emailClient" type="email" required></div>
        </div>
        <div class="ligne">
          <div class="champ"><label>Téléphone</label><input name="t" [(ngModel)]="f.telephone"></div>
          <div class="champ"><label>N° de compte (facultatif)</label><input name="c" [(ngModel)]="f.numeroCompte"></div>
        </div>
        <div class="champ"><label>Objet</label><input name="o" [(ngModel)]="f.objet" required></div>
        <div class="champ"><label>Décrivez votre problème</label><textarea name="d" [(ngModel)]="f.description" required></textarea></div>
        <button class="btn">Envoyer</button>
        @if (erreur) { <p class="erreur">{{ erreur }}</p> }
      </form>
    }
    @if (onglet === 'suivi') {
      <form class="carte ligne" (ngSubmit)="suivre()">
        <div class="champ"><label>Référence</label><input name="r" [(ngModel)]="ref" placeholder="REC-2026-000001"></div>
        <div class="champ"><label>E-mail</label><input name="m" [(ngModel)]="email"></div>
        <button class="btn">Rechercher</button>
        @if (erreur) { <p class="erreur">{{ erreur }}</p> }
      </form>
    }
    @if (suivi; as s) {
      <div class="carte resultat">
        <h2>{{ s.reference }} — {{ s.objet }}</h2>
        <p>Statut actuel : <span class="badge {{s.statut}}">{{ s.statut | libelle }}</span></p>
        <p class="sous-titre">Déposée le {{ s.dateCreation | date:'dd/MM/yyyy HH:mm' }} · réponse prévue avant le {{ s.dateEcheance | date:'dd/MM/yyyy' }}</p>
        <ol>@for (h of s.historique; track $index) { <li><b>{{ h.nouveauStatut | libelle }}</b> — {{ h.date | date:'dd/MM HH:mm' }}</li> }</ol>
        @for (c of s.reponses; track $index) { <div class="rep"><b>Réponse de la STB</b><p>{{ c.contenu }}</p></div> }
      </div>
    }
  </div>`,
  styles: [`
    header { background: var(--bleu); color: #fff; display: flex; justify-content: space-between; align-items: center; padding: 16px 32px; }
    header a { color: #fff; } .logo { font-weight: 800; font-size: 22px; } .logo span { font-weight: 400; font-size: 15px; margin-left: 8px; opacity: .85; }
    .contenu { max-width: 760px; margin: 28px auto; padding: 0 16px; }
    .onglets { display: flex; gap: 8px; margin-bottom: 14px; }
    .onglets button { flex: 1; padding: 12px; border: 1px solid var(--bord); background: #fff; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .onglets button.actif { background: var(--bleu); color: #fff; border-color: var(--bleu); }
    .ligne { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; align-items: end; }
    .resultat { margin-top: 16px; border-top: 4px solid var(--vert); }
    .rep { background: #f0fdf4; padding: 10px 14px; border-radius: 8px; margin-top: 10px; } .rep p { margin: 4px 0 0; }
  `],
})
export class PortailComponent {
  private service = inject(ReclamationService);
  onglet: 'depot' | 'suivi' = 'depot';
  f = { nomClient: '', emailClient: '', telephone: '', numeroCompte: '', objet: '', description: '', canal: 'PORTAIL_WEB' };
  ref = ''; email = ''; erreur = '';
  suivi?: SuiviPublic;

  deposer() {
    this.erreur = '';
    this.service.deposerPublic(this.f).subscribe({
      next: s => { this.suivi = s; this.ref = s.reference; this.email = this.f.emailClient; },
      error: e => this.erreur = e.error?.detail ?? 'Veuillez vérifier les champs saisis',
    });
  }
  suivre() {
    this.erreur = '';
    this.service.suivrePublic(this.ref.trim(), this.email.trim()).subscribe({
      next: s => this.suivi = s,
      error: () => { this.suivi = undefined; this.erreur = 'Aucune réclamation trouvée pour cette référence et cet e-mail'; },
    });
  }
}
