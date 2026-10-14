import { Component, Input, OnInit, inject } from '@angular/core';
import { DatePipe, PercentPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ReclamationService } from '../core/reclamation.service';
import { CATEGORIES, Reclamation, Statut } from '../core/models';
import { LibellePipe } from '../core/libelle.pipe';

@Component({
  selector: 'app-detail', standalone: true, imports: [DatePipe, PercentPipe, FormsModule, RouterLink, LibellePipe],
  template: `
  @if (r; as r) {
    <a routerLink="/reclamations" class="retour">← Retour à la liste</a>
    <div class="entete">
      <div><h1>{{ r.reference }} — {{ r.objet }}</h1>
        <p class="sous-titre">Déposée le {{ r.dateCreation | date:'dd/MM/yyyy à HH:mm' }} via {{ r.canal | libelle }}</p></div>
      <span class="badge {{r.statut}} gros">{{ r.statut | libelle }}</span>
    </div>
    <div class="grille cols">
      <div class="grille">
        <div class="carte"><h2>Description du client</h2><p class="desc">{{ r.description }}</p></div>
        <div class="carte"><h2>Actions du workflow</h2>
          @if (r.transitionsPossibles.length) {
            <div class="champ"><label>Motif / commentaire de la transition</label><input [(ngModel)]="motif" placeholder="Optionnel"></div>
            <div class="actions">
              @for (t of r.transitionsPossibles; track t) {
                <button class="btn" [class.vert]="t==='RESOLUE' || t==='CLOTUREE'" [class.rouge]="t==='REJETEE' || t==='ESCALADEE'"
                        (click)="transition(t)">→ {{ t | libelle }}</button>
              }
            </div>
          } @else { <p class="sous-titre">Réclamation terminée : aucune action possible.</p> }
          @if (erreur) { <p class="erreur">{{ erreur }}</p> }
        </div>
        <div class="carte"><h2>Échanges</h2>
          @for (c of r.commentaires; track $index) {
            <div class="com" [class.interne]="c.interne"><strong>{{ c.auteur }}</strong>
              <small>{{ c.date | date:'dd/MM HH:mm' }} · {{ c.interne ? 'Note interne' : 'Réponse au client' }}</small><p>{{ c.contenu }}</p></div>
          } @empty { <p class="sous-titre">Aucun échange pour le moment.</p> }
          <textarea [(ngModel)]="commentaire" placeholder="Rédiger une réponse ou une note interne…"></textarea>
          <div class="actions" style="margin-top:8px">
            <button class="btn" (click)="commenter(false)" [disabled]="!commentaire">Répondre au client</button>
            <button class="btn secondaire" (click)="commenter(true)" [disabled]="!commentaire">Note interne</button>
          </div>
        </div>
      </div>
      <div class="grille" style="align-content:start">
        <div class="carte"><h2>Informations</h2>
          <dl>
            <dt>Client</dt><dd>{{ r.client }}<br><small>{{ r.emailClient }}</small></dd>
            <dt>N° de compte</dt><dd>{{ r.numeroCompte || '—' }}</dd>
            <dt>Agent</dt><dd>{{ r.agent ?? 'Non affectée' }}</dd>
            <dt>Priorité</dt><dd><span class="badge {{r.priorite}}">{{ r.priorite | libelle }}</span></dd>
            <dt>Échéance SLA</dt><dd [class.retard]="r.enRetard">{{ r.dateEcheance | date:'dd/MM/yyyy HH:mm' }} @if (r.enRetard) { (dépassée) }</dd>
          </dl>
        </div>
        <div class="carte nlp"><h2>Classification NLP</h2>
          <p>Catégorie suggérée : <strong>{{ r.categorieSuggeree | libelle }}</strong>
            @if (r.confianceNlp) { <span class="badge">{{ r.confianceNlp | percent:'1.0-0' }}</span> }</p>
          <div class="champ"><label>Catégorie validée</label>
            <select [(ngModel)]="categorie">@for (c of categories; track c) { <option [value]="c">{{ c | libelle }}</option> }</select></div>
          <div class="champ"><label>Priorité</label>
            <select [(ngModel)]="priorite"><option value="HAUTE">Haute</option><option value="MOYENNE">Moyenne</option><option value="BASSE">Basse</option></select></div>
          <button class="btn secondaire" (click)="requalifier()">Valider la qualification</button>
        </div>
        <div class="carte"><h2>Historique</h2>
          <ol class="timeline">
            @for (h of r.historique; track $index) {
              <li><span class="badge {{h.nouveauStatut}}">{{ h.nouveauStatut | libelle }}</span>
                <small>{{ h.date | date:'dd/MM HH:mm' }} · {{ h.auteur }}</small>
                @if (h.motif) { <p>{{ h.motif }}</p> }</li>
            }
          </ol>
        </div>
      </div>
    </div>
  }`,
  styles: [`
    .retour { color: var(--bleu); text-decoration: none; font-size: 13px; }
    .entete { display: flex; justify-content: space-between; align-items: flex-start; margin-top: 10px; gap: 12px; }
    .gros { font-size: 14px; padding: 6px 14px; }
    .cols { grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr); }
    .desc { white-space: pre-wrap; line-height: 1.6; margin: 0; }
    .actions { display: flex; flex-wrap: wrap; gap: 8px; }
    dl { display: grid; grid-template-columns: 110px 1fr; gap: 8px 10px; margin: 0; } dt { color: var(--gris); } dd { margin: 0; }
    .nlp { border-left: 4px solid var(--vert); }
    .com { border-left: 3px solid var(--bleu); padding: 6px 12px; margin-bottom: 10px; background: #f8fafc; border-radius: 6px; }
    .com.interne { border-color: var(--orange); background: #fffbeb; } .com small { color: var(--gris); margin-left: 6px; } .com p { margin: 4px 0 0; }
    .timeline { list-style: none; padding: 0; margin: 0; } .timeline li { border-left: 2px solid var(--bord); padding: 0 0 12px 12px; }
    .timeline small { color: var(--gris); margin-left: 6px; } .timeline p { margin: 4px 0 0; font-size: 13px; }
  `],
})
export class DetailComponent implements OnInit {
  @Input() id!: string;
  private service = inject(ReclamationService);
  r?: Reclamation;
  categories = CATEGORIES;
  motif = ''; commentaire = ''; erreur = ''; categorie = ''; priorite = '';

  ngOnInit() { this.service.detail(+this.id).subscribe(r => this.maj(r)); }

  private maj(r: Reclamation) {
    this.r = r; this.erreur = '';
    this.categorie = r.categorie ?? r.categorieSuggeree ?? 'AUTRE';
    this.priorite = r.priorite;
  }

  transition(t: Statut) {
    this.service.changerStatut(this.r!.id, t, this.motif).subscribe({
      next: r => { this.maj(r); this.motif = ''; },
      error: e => this.erreur = e.error?.detail ?? 'Action refusée',
    });
  }
  commenter(interne: boolean) {
    this.service.commenter(this.r!.id, this.commentaire, interne).subscribe(r => { this.maj(r); this.commentaire = ''; });
  }
  requalifier() { this.service.requalifier(this.r!.id, this.categorie, this.priorite).subscribe(r => this.maj(r)); }
}
