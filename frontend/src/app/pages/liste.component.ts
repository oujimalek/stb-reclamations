import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, PercentPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ReclamationService } from '../core/reclamation.service';
import { CATEGORIES, Reclamation } from '../core/models';
import { LibellePipe } from '../core/libelle.pipe';

@Component({
  selector: 'app-liste', standalone: true, imports: [DatePipe, PercentPipe, FormsModule, RouterLink, LibellePipe],
  template: `
  <div class="entete">
    <div><h1>Réclamations</h1><p class="sous-titre">{{ filtrees().length }} réclamation(s)</p></div>
    <a class="btn" routerLink="/reclamations/nouvelle">+ Nouvelle réclamation</a>
  </div>
  <div class="carte filtres">
    <input placeholder="Rechercher (référence, client, objet)…" [ngModel]="recherche()" (ngModelChange)="recherche.set($event)">
    <select [ngModel]="categorie()" (ngModelChange)="categorie.set($event)">
      <option value="">Toutes les catégories</option>
      @for (c of categories; track c) { <option [value]="c">{{ c | libelle }}</option> }
    </select>
    <select [ngModel]="statut()" (ngModelChange)="statut.set($event)">
      <option value="">Tous les statuts</option>
      @for (s of statuts; track s) { <option [value]="s">{{ s | libelle }}</option> }
    </select>
  </div>
  <div class="carte" style="padding:0; overflow-x:auto">
    <table>
      <thead><tr><th>Référence</th><th>Client</th><th>Objet</th><th>Catégorie (NLP)</th><th>Priorité</th><th>Statut</th><th>Agent</th><th>Échéance</th></tr></thead>
      <tbody>
        @for (r of filtrees(); track r.id) {
          <tr class="cliquable" (click)="router.navigate(['/reclamations', r.id])">
            <td><strong>{{ r.reference }}</strong></td>
            <td>{{ r.client }}</td>
            <td class="objet">{{ r.objet }}</td>
            <td>{{ (r.categorie ?? r.categorieSuggeree) | libelle }}
              @if (r.confianceNlp) { <small class="conf">{{ r.confianceNlp | percent:'1.0-0' }}</small> }</td>
            <td><span class="badge {{r.priorite}}">{{ r.priorite | libelle }}</span></td>
            <td><span class="badge {{r.statut}}">{{ r.statut | libelle }}</span></td>
            <td>{{ r.agent ?? '—' }}</td>
            <td [class.retard]="r.enRetard">{{ r.dateEcheance | date:'dd/MM HH:mm' }}</td>
          </tr>
        } @empty { <tr><td colspan="8" style="text-align:center;color:var(--gris)">Aucune réclamation</td></tr> }
      </tbody>
    </table>
  </div>`,
  styles: [`
    .entete { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; } .entete a { text-decoration: none; }
    .filtres { display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 10px; margin-bottom: 14px; padding: 12px; }
    .objet { max-width: 260px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .conf { color: var(--gris); margin-left: 4px; }
  `],
})
export class ListeComponent implements OnInit {
  private service = inject(ReclamationService);
  router = inject(Router);
  categories = CATEGORIES;
  statuts = ['NOUVELLE', 'QUALIFIEE', 'AFFECTEE', 'EN_COURS', 'EN_ATTENTE_CLIENT', 'ESCALADEE', 'RESOLUE', 'CLOTUREE', 'REJETEE'];
  reclamations = signal<Reclamation[]>([]);
  recherche = signal(''); categorie = signal(''); statut = signal('');
  filtrees = computed(() => {
    const q = this.recherche().toLowerCase();
    return this.reclamations().filter(r =>
      (!q || `${r.reference} ${r.client} ${r.objet}`.toLowerCase().includes(q)) &&
      (!this.categorie() || (r.categorie ?? r.categorieSuggeree) === this.categorie()) &&
      (!this.statut() || r.statut === this.statut()));
  });
  ngOnInit() { this.service.lister().subscribe(l => this.reclamations.set(l)); }
}
