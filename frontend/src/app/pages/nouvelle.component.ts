import { Component, OnDestroy, inject } from '@angular/core';
import { PercentPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, debounceTime, filter, switchMap, takeUntil, catchError, of } from 'rxjs';
import { ReclamationService } from '../core/reclamation.service';
import { CANAUX } from '../core/models';
import { LibellePipe } from '../core/libelle.pipe';

@Component({
  selector: 'app-nouvelle', standalone: true, imports: [FormsModule, PercentPipe, LibellePipe],
  template: `
  <h1>Nouvelle réclamation</h1>
  <p class="sous-titre">Saisie d'une réclamation reçue en agence, par téléphone ou par e-mail.</p>
  <div class="grille cols">
    <form class="carte" (ngSubmit)="enregistrer()">
      <h2>Client</h2>
      <div class="ligne">
        <div class="champ"><label>Nom complet</label><input name="n" [(ngModel)]="f.nomClient" required></div>
        <div class="champ"><label>E-mail</label><input name="e" [(ngModel)]="f.emailClient" type="email" required></div>
      </div>
      <div class="ligne">
        <div class="champ"><label>Téléphone</label><input name="t" [(ngModel)]="f.telephone"></div>
        <div class="champ"><label>N° de compte</label><input name="c" [(ngModel)]="f.numeroCompte"></div>
      </div>
      <h2>Réclamation</h2>
      <div class="champ"><label>Canal de réception</label>
        <select name="canal" [(ngModel)]="f.canal">@for (c of canaux; track c) { <option [value]="c">{{ c | libelle }}</option> }</select></div>
      <div class="champ"><label>Objet</label><input name="o" [(ngModel)]="f.objet" (ngModelChange)="texte$.next()" required></div>
      <div class="champ"><label>Description</label><textarea name="d" [(ngModel)]="f.description" (ngModelChange)="texte$.next()" required></textarea></div>
      <button class="btn">Enregistrer la réclamation</button>
      @if (erreur) { <p class="erreur">{{ erreur }}</p> }
    </form>
    <div class="carte apercu">
      <h2>Analyse NLP en temps réel</h2>
      @if (apercu) {
        <p>Catégorie détectée</p><div class="cat">{{ apercu.categorie | libelle }}</div>
        <p>Priorité estimée : <span class="badge {{apercu.priorite}}">{{ apercu.priorite | libelle }}</span></p>
        <p style="margin-top:14px">Scores de confiance</p>
        @for (a of alternatives(); track a[0]) {
          <div class="score"><span>{{ a[0] | libelle }}</span><div class="piste"><div [style.width.%]="a[1]*100"></div></div><b>{{ a[1] | percent:'1.0-0' }}</b></div>
        }
      } @else { <p class="sous-titre">Commencez à saisir la description : le modèle propose une catégorie et une priorité.</p> }
    </div>
  </div>`,
  styles: [`
    .cols { grid-template-columns: minmax(0, 3fr) minmax(260px, 2fr); align-items: start; }
    .ligne { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
    .apercu { border-top: 4px solid var(--vert); } .apercu p { color: var(--gris); margin: 6px 0; }
    .cat { font-size: 22px; font-weight: 700; color: var(--bleu); margin-bottom: 10px; }
    .score { display: flex; align-items: center; gap: 8px; margin: 6px 0; font-size: 13px; } .score span { width: 130px; }
    .piste { flex: 1; height: 10px; background: #eef2f7; border-radius: 5px; } .piste div { height: 100%; background: var(--vert); border-radius: 5px; }
  `],
})
export class NouvelleComponent implements OnDestroy {
  private service = inject(ReclamationService);
  private router = inject(Router);
  private fin$ = new Subject<void>();
  canaux = CANAUX;
  texte$ = new Subject<void>();
  f = { nomClient: '', emailClient: '', telephone: '', numeroCompte: '', objet: '', description: '', canal: 'AGENCE' };
  apercu?: { categorie: string; confianceCategorie: number; priorite: string; alternatives: Record<string, number> };
  erreur = '';

  constructor() {
    this.texte$.pipe(
      debounceTime(400),
      filter(() => (this.f.objet + this.f.description).length > 15),
      switchMap(() => this.service.apercuNlp(`${this.f.objet}. ${this.f.description}`).pipe(catchError(() => of(undefined)))),
      takeUntil(this.fin$),
    ).subscribe(a => this.apercu = a);
  }

  alternatives() { return Object.entries(this.apercu?.alternatives ?? {}); }

  enregistrer() {
    this.service.creer(this.f).subscribe({
      next: r => this.router.navigate(['/reclamations', r.id]),
      error: e => this.erreur = e.error?.detail ?? "Erreur lors de l'enregistrement",
    });
  }
  ngOnDestroy() { this.fin$.next(); }
}
