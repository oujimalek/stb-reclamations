"""
Micro-service NLP de classification des réclamations (FastAPI).

POST /api/nlp/classify  {"texte": "..."}  ->  catégorie, priorité, scores de confiance
GET  /api/nlp/health
GET  /api/nlp/metrics
"""
import json
from pathlib import Path

import joblib
import numpy as np
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

import preprocessing  # noqa: F401  (préprocesseur requis par les modèles sérialisés)

BASE = Path(__file__).parent
MODELS = BASE / "models"

app = FastAPI(title="STB Réclamations — Service NLP", version="1.0.0")
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])

modele_categorie = joblib.load(MODELS / "modele_categorie.joblib")
modele_priorite = joblib.load(MODELS / "modele_priorite.joblib")

SEUIL_CONFIANCE = 0.45  # en dessous : qualification manuelle par un agent


class Requete(BaseModel):
    texte: str = Field(..., min_length=5, max_length=5000)


class Prediction(BaseModel):
    categorie: str
    confianceCategorie: float
    priorite: str
    confiancePriorite: float
    qualificationManuelle: bool
    alternatives: dict[str, float]


def _scores(modele, texte):
    clf = modele.named_steps["clf"]
    X = modele.named_steps["features"].transform([texte])
    if hasattr(clf, "predict_proba"):
        p = clf.predict_proba(X)[0]
    else:  # SVM linéaire : softmax sur la fonction de décision
        d = clf.decision_function(X)[0]
        e = np.exp(d - d.max())
        p = e / e.sum()
    return dict(zip(clf.classes_, p.round(4).tolist()))


@app.get("/api/nlp/health")
def health():
    return {"status": "UP"}


@app.get("/api/nlp/metrics")
def metrics():
    return json.loads((MODELS / "metriques.json").read_text(encoding="utf-8"))


@app.post("/api/nlp/classify", response_model=Prediction)
def classify(req: Requete):
    texte = req.texte.strip()
    if not texte:
        raise HTTPException(400, "Texte vide")
    sc = _scores(modele_categorie, texte)
    sp = _scores(modele_priorite, texte)
    cat = max(sc, key=sc.get)
    prio = max(sp, key=sp.get)
    top = dict(sorted(sc.items(), key=lambda kv: -kv[1])[:3])
    return Prediction(
        categorie=cat, confianceCategorie=sc[cat],
        priorite=prio, confiancePriorite=sp[prio],
        qualificationManuelle=sc[cat] < SEUIL_CONFIANCE,
        alternatives=top,
    )
