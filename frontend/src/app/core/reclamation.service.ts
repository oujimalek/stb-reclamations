import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Reclamation, Statistiques, Statut, SuiviPublic } from './models';

@Injectable({ providedIn: 'root' })
export class ReclamationService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/reclamations`;

  lister() { return this.http.get<Reclamation[]>(this.api); }
  detail(id: number) { return this.http.get<Reclamation>(`${this.api}/${id}`); }
  creer(body: unknown) { return this.http.post<Reclamation>(this.api, body); }
  changerStatut(id: number, statut: Statut, motif: string) {
    return this.http.patch<Reclamation>(`${this.api}/${id}/statut`, { statut, motif });
  }
  requalifier(id: number, categorie: string, priorite: string) {
    return this.http.patch<Reclamation>(`${this.api}/${id}/qualification`, { categorie, priorite });
  }
  commenter(id: number, contenu: string, interne: boolean) {
    return this.http.post<Reclamation>(`${this.api}/${id}/commentaires`, { contenu, interne });
  }
  statistiques() { return this.http.get<Statistiques>(`${environment.apiUrl}/statistiques`); }

  deposerPublic(body: unknown) { return this.http.post<SuiviPublic>(`${environment.apiUrl}/public/reclamations`, body); }
  suivrePublic(ref: string, email: string) {
    return this.http.get<SuiviPublic>(`${environment.apiUrl}/public/reclamations/${ref}`, { params: { email } });
  }
  /** Aperçu en direct de la classification NLP pendant la saisie. */
  apercuNlp(texte: string) {
    return this.http.post<{ categorie: string; confianceCategorie: number; priorite: string; alternatives: Record<string, number> }>(
      `${environment.nlpUrl}/classify`, { texte });
  }
}
