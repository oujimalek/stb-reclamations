package tn.stb.reclamations.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.stb.reclamations.repository.UtilisateurRepository;
import tn.stb.reclamations.security.UtilisateurPrincipal;
import tn.stb.reclamations.service.ReclamationService;
import tn.stb.reclamations.web.dto.Dtos.*;
import tn.stb.reclamations.workflow.WorkflowEngine;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/reclamations")
public class ReclamationController {
    private final ReclamationService service;
    private final WorkflowEngine workflow;
    private final UtilisateurRepository utilisateurs;

    public ReclamationController(ReclamationService service, WorkflowEngine workflow, UtilisateurRepository utilisateurs) {
        this.service = service; this.workflow = workflow; this.utilisateurs = utilisateurs;
    }

    @GetMapping
    public List<ReclamationDto> lister(@AuthenticationPrincipal UtilisateurPrincipal p) {
        return service.lister(p.utilisateur()).stream().map(r -> Mapper.dto(r, workflow)).toList();
    }

    @GetMapping("/{id}")
    public ReclamationDto detail(@PathVariable Long id) {
        return Mapper.dto(service.trouver(id), workflow);
    }

    /** Saisie par un agent (réclamation reçue en agence ou par téléphone). */
    @PostMapping
    public ReclamationDto creer(@Valid @RequestBody NouvelleReclamation req) {
        return Mapper.dto(service.deposer(req), workflow);
    }

    @PatchMapping("/{id}/statut")
    public ReclamationDto changerStatut(@PathVariable Long id, @Valid @RequestBody ChangementStatut req,
                                        @AuthenticationPrincipal UtilisateurPrincipal p) {
        return Mapper.dto(service.changerStatut(id, req, p.utilisateur()), workflow);
    }

    @PatchMapping("/{id}/qualification")
    public ReclamationDto requalifier(@PathVariable Long id, @Valid @RequestBody Requalification req,
                                      @AuthenticationPrincipal UtilisateurPrincipal p) {
        return Mapper.dto(service.requalifier(id, req, p.utilisateur()), workflow);
    }

    @PostMapping("/{id}/commentaires")
    public ReclamationDto commenter(@PathVariable Long id, @Valid @RequestBody NouveauCommentaire req,
                                    @AuthenticationPrincipal UtilisateurPrincipal p) {
        return Mapper.dto(service.commenter(id, req, p.utilisateur()), workflow);
    }

    @PatchMapping("/{id}/agent/{agentId}")
    @PreAuthorize("hasAnyRole('SUPERVISEUR','ADMIN')")
    public ReclamationDto affecter(@PathVariable Long id, @PathVariable Long agentId,
                                   @AuthenticationPrincipal UtilisateurPrincipal p) {
        var agent = utilisateurs.findById(agentId).orElseThrow(() -> new NoSuchElementException("Agent introuvable"));
        return Mapper.dto(service.affecter(id, agent, p.utilisateur()), workflow);
    }
}
