package tn.stb.reclamations.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.stb.reclamations.service.ReclamationService;
import tn.stb.reclamations.web.dto.Dtos.NouvelleReclamation;
import tn.stb.reclamations.web.dto.Dtos.SuiviPublicDto;

/** Portail client : dépôt et suivi d'une réclamation sans authentification (référence + e-mail). */
@RestController
@RequestMapping("/api/public/reclamations")
public class PublicController {
    private final ReclamationService service;

    public PublicController(ReclamationService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuiviPublicDto deposer(@Valid @RequestBody NouvelleReclamation req) {
        return Mapper.suivi(service.deposer(req));
    }

    @GetMapping("/{reference}")
    public SuiviPublicDto suivre(@PathVariable String reference, @RequestParam String email) {
        var r = service.trouverParReference(reference);
        if (!r.getClient().getEmail().equalsIgnoreCase(email)) {
            throw new java.util.NoSuchElementException("Référence inconnue : " + reference);
        }
        return Mapper.suivi(r);
    }
}
