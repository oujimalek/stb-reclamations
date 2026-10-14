from preprocessing import normaliser


def test_minuscules_et_accents():
    assert normaliser("Carte BLOQUÉE à l'agence") == "carte bloquee agence"


def test_nombres_remplaces():
    assert "<num>" in normaliser("prélèvement de 250 dinars")


def test_mots_vides_supprimes():
    resultat = normaliser("Bonjour, je suis le client de la banque")
    for mot in ("bonjour", "je", "le", "de", "la"):
        assert mot not in resultat.split()


def test_texte_vide():
    assert normaliser("") == ""
