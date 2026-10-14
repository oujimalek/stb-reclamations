package tn.stb.reclamations.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.stb.reclamations.model.Reclamation;
import tn.stb.reclamations.model.Statut;
import tn.stb.reclamations.repository.ReclamationRepository;
import tn.stb.reclamations.web.dto.Dtos.StatistiquesDto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistiques")
public class StatistiquesController {
    private final ReclamationRepository repo;

    public StatistiquesController(ReclamationRepository repo) { this.repo = repo; }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISEUR','ADMIN')")
    @Transactional(readOnly = true)
    public StatistiquesDto statistiques() {
        List<Reclamation> toutes = repo.findAll();
        long ouvertes = toutes.stream().filter(r -> !r.getStatut().estTerminal() && r.getStatut() != Statut.RESOLUE).count();
        long enRetard = toutes.stream().filter(Reclamation::estEnRetard).count();
        long escaladees = toutes.stream().filter(Reclamation::isEscaladee).count();

        var resolues = toutes.stream().filter(r -> r.getDateCloture() != null && r.getStatut() == Statut.CLOTUREE).toList();
        double tauxSla = resolues.isEmpty() ? 0 :
                100.0 * resolues.stream().filter(r -> !r.getDateCloture().isAfter(r.getDateEcheance())).count() / resolues.size();

        // précision du modèle NLP : catégorie suggérée == catégorie finale validée par l'agent
        var evaluees = toutes.stream().filter(r -> r.getCategorieSuggeree() != null && r.getCategorie() != null).toList();
        double precision = evaluees.isEmpty() ? 0 :
                100.0 * evaluees.stream().filter(r -> r.getCategorie() == r.getCategorieSuggeree()).count() / evaluees.size();

        Map<String, Long> parStatut = new LinkedHashMap<>();
        repo.compterParStatut().forEach(o -> parStatut.put(String.valueOf(o[0]), (Long) o[1]));
        Map<String, Long> parCategorie = new LinkedHashMap<>();
        repo.compterParCategorie().forEach(o -> parCategorie.put(String.valueOf(o[0]), (Long) o[1]));

        return new StatistiquesDto(toutes.size(), ouvertes, enRetard, escaladees,
                Math.round(tauxSla * 10) / 10.0, Math.round(precision * 10) / 10.0, parStatut, parCategorie);
    }
}
