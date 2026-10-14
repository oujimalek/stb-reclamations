package tn.stb.reclamations.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.stb.reclamations.config.AppProperties;
import tn.stb.reclamations.model.*;
import tn.stb.reclamations.nlp.NlpClient;
import tn.stb.reclamations.repository.ClientRepository;
import tn.stb.reclamations.repository.ReclamationRepository;
import tn.stb.reclamations.web.dto.Dtos.*;
import tn.stb.reclamations.workflow.WorkflowEngine;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ReclamationService {
    private final ReclamationRepository reclamations;
    private final ClientRepository clients;
    private final NlpClient nlp;
    private final WorkflowEngine workflow;
    private final AffectationService affectation;
    private final NotificationService notifications;
    private final AppProperties props;

    public ReclamationService(ReclamationRepository reclamations, ClientRepository clients, NlpClient nlp,
                              WorkflowEngine workflow, AffectationService affectation,
                              NotificationService notifications, AppProperties props) {
        this.reclamations = reclamations; this.clients = clients; this.nlp = nlp; this.workflow = workflow;
        this.affectation = affectation; this.notifications = notifications; this.props = props;
    }

    /**
     * Dépôt d'une réclamation : classification NLP, calcul de l'échéance SLA,
     * puis qualification et affectation automatiques si le modèle est suffisamment confiant.
     */
    public Reclamation deposer(NouvelleReclamation req) {
        Client client = clients.findByEmail(req.emailClient())
                .orElseGet(() -> clients.save(new Client(req.nomClient(), req.emailClient(), req.telephone(), req.numeroCompte())));

        Reclamation r = new Reclamation();
        r.setObjet(req.objet());
        r.setDescription(req.description());
        r.setCanal(req.canal() == null ? Canal.PORTAIL_WEB : req.canal());
        r.setClient(client);
        r.setPriorite(Priorite.MOYENNE);
        r = reclamations.save(r);
        r.setReference(String.format("REC-%d-%06d", Year.now().getValue(), r.getId()));
        historiser(r, null, Statut.NOUVELLE, "système", "Dépôt via " + r.getCanal());

        final Reclamation rec = r;
        nlp.classer(req.objet() + ". " + req.description()).ifPresent(p -> {
            rec.setCategorieSuggeree(Categorie.valueOf(p.categorie()));
            rec.setConfianceNlp(p.confianceCategorie());
            rec.setPriorite(Priorite.valueOf(p.priorite()));
            if (!p.qualificationManuelle() && p.confianceCategorie() >= props.nlp().seuilConfiance()) {
                rec.setCategorie(rec.getCategorieSuggeree());
                changerStatutInterne(rec, Statut.QUALIFIEE, "système (NLP)",
                        String.format("Classée %s (confiance %.0f %%)", p.categorie(), p.confianceCategorie() * 100));
            }
        });
        rec.setDateEcheance(LocalDateTime.now().plusHours(props.slaHeures().getOrDefault(rec.getPriorite(), 72)));
        if (rec.getStatut() == Statut.QUALIFIEE) {
            affecterAutomatiquement(rec);
        }
        notifications.notifierClient(rec, "Accusé de réception",
                "Votre réclamation a bien été enregistrée sous la référence " + rec.getReference() + ".");
        return rec;
    }

    private void affecterAutomatiquement(Reclamation r) {
        affectation.choisirAgent(r.getCategorie()).ifPresent(agent -> {
            r.setAgent(agent);
            changerStatutInterne(r, Statut.AFFECTEE, "système", "Affectée à " + agent.getNomComplet());
        });
    }

    public Reclamation requalifier(Long id, Requalification req, Utilisateur auteur) {
        Reclamation r = trouver(id);
        r.setCategorie(req.categorie());
        if (req.priorite() != null) {
            r.setPriorite(req.priorite());
            r.setDateEcheance(r.getDateCreation().plusHours(props.slaHeures().getOrDefault(req.priorite(), 72)));
        }
        if (r.getStatut() == Statut.NOUVELLE) {
            changerStatut(r, Statut.QUALIFIEE, auteur, "Qualification manuelle : " + req.categorie());
            affecterAutomatiquement(r);
        }
        return r;
    }

    public Reclamation changerStatut(Long id, ChangementStatut req, Utilisateur auteur) {
        return changerStatut(trouver(id), req.statut(), auteur, req.motif());
    }

    private Reclamation changerStatut(Reclamation r, Statut vers, Utilisateur auteur, String motif) {
        workflow.verifier(r.getStatut(), vers, auteur.getRole());
        changerStatutInterne(r, vers, auteur.getNomComplet(), motif);
        if (vers == Statut.RESOLUE || vers == Statut.REJETEE || vers == Statut.EN_ATTENTE_CLIENT) {
            notifications.notifierClient(r, "Mise à jour de votre réclamation",
                    "Statut : " + vers + (motif == null ? "" : "\n" + motif));
        }
        return r;
    }

    public void changerStatutInterne(Reclamation r, Statut vers, String auteur, String motif) {
        Statut ancien = r.getStatut();
        r.setStatut(vers);
        if (vers.estTerminal()) r.setDateCloture(LocalDateTime.now());
        if (vers == Statut.ESCALADEE) r.setEscaladee(true);
        historiser(r, ancien, vers, auteur, motif);
    }

    private void historiser(Reclamation r, Statut ancien, Statut nouveau, String auteur, String motif) {
        r.getHistorique().add(new HistoriqueStatut(r, ancien, nouveau, auteur, motif));
    }

    public Reclamation commenter(Long id, NouveauCommentaire req, Utilisateur auteur) {
        Reclamation r = trouver(id);
        r.getCommentaires().add(new Commentaire(r, auteur.getNomComplet(), req.contenu(), req.interne()));
        if (!req.interne()) {
            notifications.notifierClient(r, "Réponse à votre réclamation", req.contenu());
        }
        return r;
    }

    public Reclamation affecter(Long id, Utilisateur agent, Utilisateur auteur) {
        Reclamation r = trouver(id);
        r.setAgent(agent);
        if (r.getStatut() == Statut.QUALIFIEE) {
            changerStatut(r, Statut.AFFECTEE, auteur, "Affectation manuelle à " + agent.getNomComplet());
        }
        return r;
    }

    @Transactional(readOnly = true)
    public Reclamation trouver(Long id) {
        return reclamations.findById(id).orElseThrow(() -> new NoSuchElementException("Réclamation introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Reclamation trouverParReference(String ref) {
        return reclamations.findByReference(ref).orElseThrow(() -> new NoSuchElementException("Référence inconnue : " + ref));
    }

    @Transactional(readOnly = true)
    public List<Reclamation> lister(Utilisateur u) {
        return u.getRole() == Role.AGENT
                ? reclamations.findByAgentOrderByDateCreationDesc(u)
                : reclamations.findAllByOrderByDateCreationDesc();
    }
}
