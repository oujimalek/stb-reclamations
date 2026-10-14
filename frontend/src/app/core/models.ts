export type Statut = 'NOUVELLE' | 'QUALIFIEE' | 'AFFECTEE' | 'EN_COURS' | 'EN_ATTENTE_CLIENT' | 'ESCALADEE' | 'RESOLUE' | 'CLOTUREE' | 'REJETEE';
export type Priorite = 'HAUTE' | 'MOYENNE' | 'BASSE';
export const CATEGORIES = ['CARTE_BANCAIRE', 'VIREMENT', 'CREDIT', 'COMPTE', 'FRAIS_COMMISSIONS', 'BANQUE_DIGITALE', 'CHEQUE', 'ACCUEIL_AGENCE', 'AUTRE'];
export const CANAUX = ['AGENCE', 'CENTRE_APPEL', 'EMAIL', 'PORTAIL_WEB', 'APPLICATION_MOBILE'];

export interface Historique { ancienStatut: Statut | null; nouveauStatut: Statut; auteur: string; motif: string; date: string; }
export interface Commentaire { auteur: string; contenu: string; interne: boolean; date: string; }

export interface Reclamation {
  id: number; reference: string; objet: string; description: string; canal: string;
  categorie: string | null; categorieSuggeree: string | null; confianceNlp: number | null;
  priorite: Priorite; statut: Statut; client: string; emailClient: string; numeroCompte: string;
  agent: string | null; dateCreation: string; dateEcheance: string; dateCloture: string | null;
  escaladee: boolean; enRetard: boolean; transitionsPossibles: Statut[];
  historique: Historique[]; commentaires: Commentaire[];
}

export interface SuiviPublic {
  reference: string; objet: string; statut: Statut; dateCreation: string; dateEcheance: string;
  historique: Historique[]; reponses: Commentaire[];
}

export interface Statistiques {
  total: number; ouvertes: number; enRetard: number; escaladees: number;
  tauxResolutionDansSla: number; precisionNlp: number;
  parStatut: Record<string, number>; parCategorie: Record<string, number>;
}

export interface Session { token: string; nomComplet: string; email: string; role: 'AGENT' | 'SUPERVISEUR' | 'ADMIN'; }

export const LIBELLES: Record<string, string> = {
  NOUVELLE: 'Nouvelle', QUALIFIEE: 'Qualifiée', AFFECTEE: 'Affectée', EN_COURS: 'En cours',
  EN_ATTENTE_CLIENT: 'En attente client', ESCALADEE: 'Escaladée', RESOLUE: 'Résolue', CLOTUREE: 'Clôturée', REJETEE: 'Rejetée',
  CARTE_BANCAIRE: 'Carte bancaire', VIREMENT: 'Virement', CREDIT: 'Crédit', COMPTE: 'Compte',
  FRAIS_COMMISSIONS: 'Frais & commissions', BANQUE_DIGITALE: 'Banque digitale', CHEQUE: 'Chèque',
  ACCUEIL_AGENCE: 'Accueil / agence', AUTRE: 'Autre', HAUTE: 'Haute', MOYENNE: 'Moyenne', BASSE: 'Basse',
  AGENCE: 'Agence', CENTRE_APPEL: "Centre d'appel", EMAIL: 'E-mail', PORTAIL_WEB: 'Portail web', APPLICATION_MOBILE: 'Application mobile',
};
