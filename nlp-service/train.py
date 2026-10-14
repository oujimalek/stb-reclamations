"""
Entraînement et évaluation des modèles de classification des réclamations.

Pipeline : prétraitement -> TF-IDF (mots + n-grammes de caractères) -> classifieur.
Trois classifieurs sont comparés ; le meilleur (F1 macro) est sauvegardé.
"""
import json
from pathlib import Path

import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, confusion_matrix, f1_score, accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.naive_bayes import ComplementNB
from sklearn.pipeline import Pipeline, FeatureUnion
from sklearn.svm import LinearSVC
from sklearn.base import clone

BASE = Path(__file__).parent
MODELS = BASE / "models"
MODELS.mkdir(exist_ok=True)

from preprocessing import normaliser


def features():
    return FeatureUnion([
        ("mots", TfidfVectorizer(preprocessor=normaliser, ngram_range=(1, 2), min_df=2, sublinear_tf=True)),
        ("chars", TfidfVectorizer(preprocessor=normaliser, analyzer="char_wb", ngram_range=(3, 5), min_df=2, sublinear_tf=True)),
    ])


CANDIDATS = {
    "Naive Bayes (Complement)": ComplementNB(),
    "Régression logistique": LogisticRegression(max_iter=2000, C=5),
    "SVM linéaire": LinearSVC(C=1.0),
}


def entrainer(df, cible):
    X_train, X_test, y_train, y_test = train_test_split(
        df["texte"], df[cible], test_size=0.2, random_state=42, stratify=df[cible])
    resultats, meilleur, meilleur_f1 = {}, None, -1
    for nom, clf in CANDIDATS.items():
        pipe = Pipeline([("features", features()), ("clf", clone(clf))])
        pipe.fit(X_train, y_train)
        pred = pipe.predict(X_test)
        f1 = f1_score(y_test, pred, average="macro")
        resultats[nom] = {"accuracy": round(accuracy_score(y_test, pred), 4), "f1_macro": round(f1, 4)}
        print(f"[{cible}] {nom:28s} acc={resultats[nom]['accuracy']:.4f} f1={f1:.4f}")
        if f1 > meilleur_f1:
            meilleur, meilleur_f1, meilleur_nom, meilleur_pred = pipe, f1, nom, pred
    labels = sorted(df[cible].unique())
    rapport = classification_report(y_test, meilleur_pred, output_dict=True, zero_division=0)
    cm = confusion_matrix(y_test, meilleur_pred, labels=labels).tolist()
    return meilleur, {
        "comparaison": resultats, "meilleur": meilleur_nom, "labels": labels,
        "rapport": rapport, "matrice_confusion": cm,
        "n_train": len(X_train), "n_test": len(X_test),
    }


if __name__ == "__main__":
    df = pd.read_csv(BASE / "data" / "reclamations.csv")
    print(f"Corpus : {len(df)} réclamations")
    m_cat, met_cat = entrainer(df, "categorie")
    m_prio, met_prio = entrainer(df, "priorite")
    # Évaluation de généralisation sur un jeu rédigé à la main (formulations jamais vues)
    manuel = pd.read_csv(BASE / "data" / "test_manuel.csv")
    pred_m = m_cat.predict(manuel["texte"])
    met_cat["test_manuel"] = {
        "n": len(manuel),
        "accuracy": round(accuracy_score(manuel["categorie"], pred_m), 4),
        "f1_macro": round(f1_score(manuel["categorie"], pred_m, average="macro"), 4),
        "erreurs": [{"texte": t, "attendu": a, "predit": p}
                    for t, a, p in zip(manuel["texte"], manuel["categorie"], pred_m) if a != p],
    }
    print("Test manuel :", met_cat["test_manuel"]["accuracy"], met_cat["test_manuel"]["f1_macro"])
    joblib.dump(m_cat, MODELS / "modele_categorie.joblib")
    joblib.dump(m_prio, MODELS / "modele_priorite.joblib")
    (MODELS / "metriques.json").write_text(
        json.dumps({"categorie": met_cat, "priorite": met_prio}, ensure_ascii=False, indent=2), encoding="utf-8")
    print("Modèles et métriques sauvegardés dans", MODELS)
