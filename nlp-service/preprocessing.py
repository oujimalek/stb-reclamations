"""Prétraitement du texte des réclamations."""
import re
import unicodedata

STOPWORDS_FR = {
    "le", "la", "les", "de", "des", "du", "un", "une", "et", "a", "à", "est", "en", "que", "qui", "je", "j", "ma",
    "mon", "mes", "pour", "par", "sur", "au", "aux", "ce", "il", "elle", "pas", "ne", "me", "m", "l", "d", "vous",
    "bonjour", "madame", "monsieur", "merci", "cordialement", "salut",
}


def normaliser(texte: str) -> str:
    texte = texte.lower()
    texte = unicodedata.normalize("NFD", texte)
    texte = "".join(c for c in texte if unicodedata.category(c) != "Mn")  # retire les accents
    texte = re.sub(r"\d+", " <num> ", texte)
    texte = re.sub(r"[^a-z<> ]", " ", texte)
    return " ".join(t for t in texte.split() if t not in STOPWORDS_FR)
