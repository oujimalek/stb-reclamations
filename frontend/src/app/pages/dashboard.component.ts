import { Component, OnInit, inject } from '@angular/core';
import { KeyValuePipe } from '@angular/common';
import { ReclamationService } from '../core/reclamation.service';
import { AuthService } from '../core/auth.service';
import { Statistiques } from '../core/models';
import { LibellePipe } from '../core/libelle.pipe';

@Component({
  selector: 'app-dashboard', standalone: true, imports: [KeyValuePipe, LibellePipe],
  template: `
  <h1>Tableau de bord</h1>
  <p class="sous-titre">Suivi global du traitement des réclamations et de la performance du modèle NLP.</p>
  @if (stats) {
    <div class="grille kpis">
      <div class="carte kpi"><span>Total</span><strong>{{ stats.total }}</strong></div>
      <div class="carte kpi"><span>Ouvertes</span><strong>{{ stats.ouvertes }}</strong></div>
      <div class="carte kpi rouge"><span>En retard (SLA)</span><strong>{{ stats.enRetard }}</strong></div>
      <div class="carte kpi"><span>Escaladées</span><strong>{{ stats.escaladees }}</strong></div>
      <div class="carte kpi vert"><span>Clôturées dans le SLA</span><strong>{{ stats.tauxResolutionDansSla }} %</strong></div>
      <div class="carte kpi bleu"><span>Précision NLP (validée)</span><strong>{{ stats.precisionNlp }} %</strong></div>
    </div>
    <div class="grille deux">
      <div class="carte"><h2>Répartition par catégorie</h2>
        @for (c of stats.parCategorie | keyvalue; track c.key) {
          <div class="barre"><span class="lib">{{ c.key | libelle }}</span>
            <div class="piste"><div class="rempli" [style.width.%]="100 * c.value / max(stats.parCategorie)"></div></div>
            <span class="val">{{ c.value }}</span></div>
        }
      </div>
      <div class="carte"><h2>Répartition par statut</h2>
        @for (s of stats.parStatut | keyvalue; track s.key) {
          <div class="barre"><span class="lib"><span class="badge {{s.key}}">{{ s.key | libelle }}</span></span>
            <div class="piste"><div class="rempli vert" [style.width.%]="100 * s.value / max(stats.parStatut)"></div></div>
            <span class="val">{{ s.value }}</span></div>
        }
      </div>
    </div>
  } @else if (!auth.estSuperviseur()) {
    <div class="carte">Le tableau de bord global est réservé aux superviseurs. Consultez vos réclamations dans l'onglet « Réclamations ».</div>
  }`,
  styles: [`
    .kpis { grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); margin-bottom: 16px; }
    .kpi span { color: var(--gris); font-size: 12px; text-transform: uppercase; } .kpi strong { display: block; font-size: 28px; margin-top: 6px; color: var(--bleu); }
    .kpi.rouge strong { color: var(--rouge); } .kpi.vert strong { color: var(--vert); }
    .deux { grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); }
    .barre { display: flex; align-items: center; gap: 10px; margin: 9px 0; }
    .lib { width: 150px; font-size: 13px; } .val { width: 30px; text-align: right; font-weight: 600; }
    .piste { flex: 1; background: #eef2f7; border-radius: 6px; height: 12px; }
    .rempli { background: var(--bleu); height: 100%; border-radius: 6px; } .rempli.vert { background: var(--vert); }
  `],
})
export class DashboardComponent implements OnInit {
  private service = inject(ReclamationService);
  auth = inject(AuthService);
  stats?: Statistiques;
  ngOnInit() { if (this.auth.estSuperviseur()) this.service.statistiques().subscribe(s => this.stats = s); }
  max(m: Record<string, number>) { return Math.max(1, ...Object.values(m)); }
}
