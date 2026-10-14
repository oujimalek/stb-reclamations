package tn.stb.reclamations.service;

import org.junit.jupiter.api.Test;
import tn.stb.reclamations.model.Categorie;
import tn.stb.reclamations.model.Role;
import tn.stb.reclamations.model.Utilisateur;
import tn.stb.reclamations.repository.ReclamationRepository;
import tn.stb.reclamations.repository.UtilisateurRepository;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AffectationServiceTest {

    private final UtilisateurRepository utilisateurs = mock(UtilisateurRepository.class);
    private final ReclamationRepository reclamations = mock(ReclamationRepository.class);
    private final AffectationService service = new AffectationService(utilisateurs, reclamations);

    private final Utilisateur karim = new Utilisateur("k@stb.com.tn", "x", "Karim", Role.AGENT, Set.of(Categorie.CARTE_BANCAIRE));
    private final Utilisateur sami = new Utilisateur("s@stb.com.tn", "x", "Sami", Role.AGENT, Set.of(Categorie.CARTE_BANCAIRE, Categorie.CREDIT));
    private final Utilisateur ines = new Utilisateur("i@stb.com.tn", "x", "Ines", Role.AGENT, Set.of(Categorie.VIREMENT));

    @Test
    void choisitLAgentHabiliteLeMoinsCharge() {
        when(utilisateurs.findByRoleAndActifTrue(Role.AGENT)).thenReturn(List.of(karim, sami, ines));
        when(reclamations.countByAgentAndStatutNotIn(eq(karim), any())).thenReturn(5L);
        when(reclamations.countByAgentAndStatutNotIn(eq(sami), any())).thenReturn(2L);
        when(reclamations.countByAgentAndStatutNotIn(eq(ines), any())).thenReturn(0L);

        assertEquals(sami, service.choisirAgent(Categorie.CARTE_BANCAIRE).orElseThrow(),
                "Ines est la moins chargée mais n'est pas habilitée sur les cartes");
    }

    @Test
    void aucunAgentHabilite_retourneVide() {
        when(utilisateurs.findByRoleAndActifTrue(Role.AGENT)).thenReturn(List.of(karim, ines));

        assertTrue(service.choisirAgent(Categorie.FRAIS_COMMISSIONS).isEmpty());
    }
}
