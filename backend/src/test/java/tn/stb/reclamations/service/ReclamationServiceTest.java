package tn.stb.reclamations.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tn.stb.reclamations.config.AppProperties;
import tn.stb.reclamations.model.*;
import tn.stb.reclamations.nlp.NlpClient;
import tn.stb.reclamations.repository.ClientRepository;
import tn.stb.reclamations.repository.ReclamationRepository;
import tn.stb.reclamations.web.dto.Dtos.ChangementStatut;
import tn.stb.reclamations.web.dto.Dtos.NouvelleReclamation;
import tn.stb.reclamations.workflow.TransitionInvalideException;
import tn.stb.reclamations.workflow.WorkflowEngine;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReclamationServiceTest {

    @Mock ReclamationRepository reclamations;
    @Mock ClientRepository clients;
    @Mock NlpClient nlp;
    @Mock AffectationService affectation;
    @Mock NotificationService notifications;

    ReclamationService service;
    Utilisateur agent;

    @BeforeEach
    void init() {
        AppProperties props = new AppProperties(
                new AppProperties.Jwt("c2VjcmV0LWRlLXRlc3QtcG91ci1sZXMtdGVzdHMtdW5pdGFpcmVzLTIwMjY=", 60),
                new AppProperties.Nlp("http://localhost:8000", 0.45),
                Map.of(Priorite.HAUTE, 24, Priorite.MOYENNE, 72, Priorite.BASSE, 120),
                new AppProperties.Cors("http://localhost:4200"),
                new AppProperties.Notifications(false));
        service = new ReclamationService(reclamations, clients, nlp, new WorkflowEngine(), affectation, notifications, props);
        agent = new Utilisateur("agent@stb.com.tn", "x", "Karim Trabelsi", Role.AGENT, Set.of(Categorie.CARTE_BANCAIRE));

        lenient().when(clients.findByEmail(anyString())).thenReturn(Optional.empty());
        lenient().when(clients.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(reclamations.save(any(Reclamation.class))).thenAnswer(inv -> {
            Reclamation r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 42L);
            return r;
        });
    }

    private NouvelleReclamation demande() {
        return new NouvelleReclamation("Hela Bouzid", "hela@test.tn", "22000000", "0123",
                "Carte avalée", "Ma carte a été avalée par le distributeur de l'agence", Canal.PORTAIL_WEB);
    }

    @Test
    void depotAvecConfianceElevee_qualifieEtAffecteAutomatiquement() {
        when(nlp.classer(anyString())).thenReturn(Optional.of(
                new NlpClient.Prediction("CARTE_BANCAIRE", 0.95, "HAUTE", 0.9, false, Map.of())));
        when(affectation.choisirAgent(Categorie.CARTE_BANCAIRE)).thenReturn(Optional.of(agent));

        Reclamation r = service.deposer(demande());

        assertEquals(Statut.AFFECTEE, r.getStatut());
        assertEquals(Categorie.CARTE_BANCAIRE, r.getCategorie());
        assertEquals(Priorite.HAUTE, r.getPriorite());
        assertEquals(agent, r.getAgent());
        assertEquals(String.format("REC-%d-000042", Year.now().getValue()), r.getReference());
        assertEquals(3, r.getHistorique().size(), "NOUVELLE -> QUALIFIEE -> AFFECTEE");
        long heures = Duration.between(LocalDateTime.now(), r.getDateEcheance()).toHours();
        assertTrue(heures >= 23 && heures <= 24, "SLA de 24 h pour une priorité haute");
        verify(notifications).notifierClient(eq(r), eq("Accusé de réception"), anyString());
    }

    @Test
    void depotSansServiceNlp_resteNouvellePourQualificationManuelle() {
        when(nlp.classer(anyString())).thenReturn(Optional.empty());

        Reclamation r = service.deposer(demande());

        assertEquals(Statut.NOUVELLE, r.getStatut());
        assertNull(r.getCategorie());
        assertEquals(Priorite.MOYENNE, r.getPriorite());
        verifyNoInteractions(affectation);
    }

    @Test
    void depotAvecConfianceFaible_conserveLaSuggestionSansQualifier() {
        when(nlp.classer(anyString())).thenReturn(Optional.of(
                new NlpClient.Prediction("COMPTE", 0.30, "BASSE", 0.8, true, Map.of())));

        Reclamation r = service.deposer(demande());

        assertEquals(Statut.NOUVELLE, r.getStatut());
        assertEquals(Categorie.COMPTE, r.getCategorieSuggeree());
        assertNull(r.getCategorie());
    }

    @Test
    void transitionInterdite_estRefusee() {
        Reclamation r = new Reclamation();
        r.setStatut(Statut.NOUVELLE);
        when(reclamations.findById(1L)).thenReturn(Optional.of(r));

        assertThrows(TransitionInvalideException.class,
                () -> service.changerStatut(1L, new ChangementStatut(Statut.CLOTUREE, "test"), agent));
        assertEquals(Statut.NOUVELLE, r.getStatut());
    }

    @Test
    void resolution_historiseEtNotifieLeClient() {
        Reclamation r = new Reclamation();
        r.setStatut(Statut.EN_COURS);
        r.setClient(new Client("Hela", "hela@test.tn", null, null));
        when(reclamations.findById(1L)).thenReturn(Optional.of(r));

        service.changerStatut(1L, new ChangementStatut(Statut.RESOLUE, "Frais remboursés"), agent);

        assertEquals(Statut.RESOLUE, r.getStatut());
        assertEquals(1, r.getHistorique().size());
        assertEquals("Frais remboursés", r.getHistorique().get(0).getMotif());
        verify(notifications).notifierClient(eq(r), anyString(), contains("RESOLUE"));
    }

    @Test
    void cloture_renseigneLaDateDeCloture() {
        Reclamation r = new Reclamation();
        r.setStatut(Statut.RESOLUE);
        when(reclamations.findById(1L)).thenReturn(Optional.of(r));

        service.changerStatut(1L, new ChangementStatut(Statut.CLOTUREE, null), agent);

        assertNotNull(r.getDateCloture());
    }
}
