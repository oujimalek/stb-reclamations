"""Garde-fous de non-régression sur la qualité du modèle."""
import json
from pathlib import Path

METRIQUES = json.loads((Path(__file__).resolve().parent.parent / "models" / "metriques.json").read_text(encoding="utf-8"))


def test_f1_categorie_minimum():
    cat = METRIQUES["categorie"]
    assert cat["comparaison"][cat["meilleur"]]["f1_macro"] >= 0.90


def test_generalisation_minimum():
    assert METRIQUES["categorie"]["test_manuel"]["accuracy"] >= 0.65


def test_f1_priorite_minimum():
    pr = METRIQUES["priorite"]
    assert pr["comparaison"][pr["meilleur"]]["f1_macro"] >= 0.85
