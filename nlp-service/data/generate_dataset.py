"""
Génération d'un jeu de données synthétique de réclamations bancaires (français).

Les réclamations réelles des clients étant confidentielles (secret bancaire),
ce script produit un corpus anonyme et représentatif à partir de gabarits
validés avec l'équipe métier. Chaque réclamation a une catégorie et une priorité.
"""
import csv
import random
from pathlib import Path

random.seed(42)

CATEGORIES = {
    "CARTE_BANCAIRE": {
        "sujets": ["ma carte", "ma carte Visa", "ma carte bancaire", "ma carte CIB", "ma carte Mastercard", "la carte de mon compte"],
        "problemes": [
            "a été avalée par le distributeur de l'agence {agence}",
            "est bloquée depuis {jours} jours sans explication",
            "ne fonctionne pas pour les paiements en ligne",
            "a été refusée chez un commerçant alors que le solde est suffisant",
            "n'est toujours pas disponible alors que je l'ai commandée il y a {jours} jours",
            "a été débitée deux fois pour le même achat de {montant} dinars",
            "affiche un code PIN erroné alors que je tape le bon code",
            "a expiré et je n'ai pas reçu la nouvelle",
            "a été utilisée pour un retrait que je n'ai pas effectué",
        ],
    },
    "VIREMENT": {
        "sujets": ["mon virement", "le virement que j'ai émis", "le virement de mon salaire", "un virement international", "mon virement permanent"],
        "problemes": [
            "de {montant} dinars n'est jamais arrivé chez le bénéficiaire",
            "est en attente depuis {jours} jours",
            "a été rejeté sans motif",
            "a été exécuté deux fois",
            "n'a pas été crédité sur mon compte à la date prévue",
            "vers l'étranger est bloqué au niveau de la conformité",
            "a été envoyé vers un mauvais RIB par erreur de l'agence",
        ],
    },
    "CREDIT": {
        "sujets": ["ma demande de crédit", "mon crédit immobilier", "mon crédit consommation", "le dossier de prêt", "mon échéance de crédit"],
        "problemes": [
            "est en cours d'étude depuis {jours} jours sans réponse",
            "a été prélevée deux fois ce mois-ci",
            "présente un taux différent de celui annoncé",
            "a été refusée sans explication claire",
            "n'a pas été débloquée alors que tous les documents sont fournis",
            "ne tient pas compte de mon remboursement anticipé",
            "comporte des pénalités de retard injustifiées",
        ],
    },
    "COMPTE": {
        "sujets": ["mon compte courant", "mon compte épargne", "mon compte", "le compte de ma société", "mon compte joint"],
        "problemes": [
            "a été bloqué sans préavis",
            "affiche un solde incorrect",
            "n'est toujours pas ouvert après {jours} jours",
            "ne peut pas être clôturé malgré ma demande",
            "présente une opération de {montant} dinars que je ne reconnais pas",
            "n'a pas reçu les intérêts prévus",
            "a été débité sans autorisation",
        ],
    },
    "FRAIS_COMMISSIONS": {
        "sujets": ["des frais", "une commission", "des agios", "les frais de tenue de compte", "une commission de {montant} dinars"],
        "problemes": [
            "ont été prélevés sans que je sois informé",
            "a été facturée deux fois",
            "sont trop élevés par rapport aux conditions affichées",
            "ont été appliqués alors que mon compte n'était pas à découvert",
            "a été prélevée sur un service que je n'ai jamais demandé",
            "ne correspondent pas à la grille tarifaire",
        ],
    },
    "BANQUE_DIGITALE": {
        "sujets": ["l'application mobile", "le site e-banking", "l'application STB", "mon accès en ligne", "le service SMS"],
        "problemes": [
            "ne fonctionne plus depuis la dernière mise à jour",
            "m'affiche une erreur à la connexion",
            "ne m'envoie pas le code OTP",
            "refuse mon mot de passe alors qu'il est correct",
            "est très lente et se bloque",
            "n'affiche pas mes dernières opérations",
            "a été bloqué après trois tentatives",
        ],
    },
    "CHEQUE": {
        "sujets": ["mon chéquier", "le chèque que j'ai déposé", "un chèque de {montant} dinars", "ma demande de chéquier", "le chèque remis à l'encaissement"],
        "problemes": [
            "n'est toujours pas disponible après {jours} jours",
            "n'a pas été crédité sur mon compte",
            "a été rejeté à tort",
            "a été perdu par l'agence",
            "a été encaissé deux fois",
            "a été débité avant la date indiquée",
        ],
    },
    "ACCUEIL_AGENCE": {
        "sujets": ["l'accueil à l'agence {agence}", "le conseiller de l'agence {agence}", "le service client", "le centre d'appel", "l'agence {agence}"],
        "problemes": [
            "a été désagréable et peu professionnel",
            "ne répond jamais au téléphone",
            "m'a fait attendre plus de deux heures",
            "ne donne aucune information sur mon dossier",
            "était fermé pendant les horaires d'ouverture",
            "m'a donné des informations contradictoires",
        ],
    },
}

OUVERTURES = [
    "Bonjour,", "Bonjour madame, monsieur,", "Madame, Monsieur,", "Salut,", "", "", "Je vous écris car",
    "Je souhaite signaler que", "Je tiens à me plaindre :", "Réclamation :",
]
FERMETURES = [
    "Merci de traiter ma demande rapidement.", "Je demande un remboursement.",
    "Merci de me rappeler.", "C'est inacceptable.", "Merci d'avance.", "", "",
    "Je compte changer de banque si rien n'est fait.", "Cordialement.",
    "J'attends une réponse urgente.",
]
AGENCES = ["Tunis Centre", "Ariana", "Ben Arous", "Sousse", "Sfax", "La Marsa", "Nabeul", "Bizerte", "Monastir"]

URGENT_MARKERS = ["urgent", "inacceptable", "fraude", "que je n'ai pas effectué", "que je ne reconnais pas",
                  "sans autorisation", "avalée", "bloqué", "changer de banque", "deux fois", "jamais arrivé"]


def priorite(texte: str) -> str:
    t = texte.lower()
    score = sum(1 for m in URGENT_MARKERS if m in t)
    if score >= 2:
        return "HAUTE"
    if score == 1:
        return "MOYENNE"
    return "BASSE"


def generer(n_par_categorie: int = 220):
    lignes = []
    for cat, d in CATEGORIES.items():
        for _ in range(n_par_categorie):
            sujet = random.choice(d["sujets"])
            pb = random.choice(d["problemes"])
            phrase = f"{sujet} {pb}".format(
                agence=random.choice(AGENCES),
                jours=random.randint(2, 45),
                montant=random.choice([35, 80, 120, 250, 500, 1200, 3000]),
            )
            ouv = random.choice(OUVERTURES)
            fer = random.choice(FERMETURES)
            texte = " ".join(x for x in [ouv, phrase[0].upper() + phrase[1:] + ".", fer] if x)
            # bruit réaliste : minuscules, fautes de frappe légères
            if random.random() < 0.25:
                texte = texte.lower()
            if random.random() < 0.10:
                texte = texte.replace("é", "e")
            # 6 % d'étiquettes bruitées : simule les désaccords d'annotation entre agents
            label = random.choice(list(CATEGORIES)) if random.random() < 0.06 else cat
            lignes.append({"texte": texte, "categorie": label, "priorite": priorite(texte)})
    random.shuffle(lignes)
    return lignes


if __name__ == "__main__":
    out = Path(__file__).parent / "reclamations.csv"
    rows = generer()
    with out.open("w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=["texte", "categorie", "priorite"])
        w.writeheader()
        w.writerows(rows)
    print(f"{len(rows)} réclamations écrites dans {out}")
