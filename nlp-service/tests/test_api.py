import pytest
from fastapi.testclient import TestClient

from app import app

client = TestClient(app)


def test_health():
    r = client.get("/api/nlp/health")
    assert r.status_code == 200
    assert r.json() == {"status": "UP"}


@pytest.mark.parametrize("texte,categorie", [
    ("Ma carte bancaire a été avalée par le distributeur", "CARTE_BANCAIRE"),
    ("Le virement de mon salaire n'est jamais arrivé", "VIREMENT"),
    ("Mon échéance de crédit a été prélevée deux fois", "CREDIT"),
    ("L'application mobile m'affiche une erreur à la connexion", "BANQUE_DIGITALE"),
    ("Ma demande de chéquier n'est toujours pas disponible", "CHEQUE"),
])
def test_classification_categories(texte, categorie):
    r = client.post("/api/nlp/classify", json={"texte": texte})
    assert r.status_code == 200
    assert r.json()["categorie"] == categorie


def test_structure_reponse():
    r = client.post("/api/nlp/classify", json={"texte": "Des frais ont été prélevés sans que je sois informé"})
    body = r.json()
    assert set(body) == {"categorie", "confianceCategorie", "priorite", "confiancePriorite",
                         "qualificationManuelle", "alternatives"}
    assert 0 <= body["confianceCategorie"] <= 1
    assert body["priorite"] in {"HAUTE", "MOYENNE", "BASSE"}
    assert len(body["alternatives"]) == 3


def test_priorite_haute_pour_fraude():
    r = client.post("/api/nlp/classify",
                    json={"texte": "Un retrait que je n'ai pas effectué, compte débité sans autorisation, c'est inacceptable"})
    assert r.json()["priorite"] == "HAUTE"


def test_texte_trop_court_refuse():
    r = client.post("/api/nlp/classify", json={"texte": "abc"})
    assert r.status_code == 422


def test_metriques_disponibles():
    r = client.get("/api/nlp/metrics")
    assert r.status_code == 200
    assert "categorie" in r.json() and "priorite" in r.json()
