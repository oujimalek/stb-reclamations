package tn.stb.reclamations.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tn.stb.reclamations.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class Dtos {
    private Dtos() {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String motDePasse) {}

    public record LoginResponse(String token, String nomComplet, String email, Role role) {}

    public record NouvelleReclamation(
            @NotBlank String nomClient, @NotBlank @Email String emailClient, String telephone, String numeroCompte,
            @NotBlank @Size(max = 200) String objet, @NotBlank @Size(min = 10, max = 5000) String description,
            Canal canal) {}

    public record ChangementStatut(@NotNull Statut statut, String motif) {}

    public record NouveauCommentaire(@NotBlank String contenu, boolean interne) {}

    public record Requalification(@NotNull Categorie categorie, Priorite priorite) {}

    public record HistoriqueDto(Statut ancienStatut, Statut nouveauStatut, String auteur, String motif, LocalDateTime date) {
        public static HistoriqueDto de(HistoriqueStatut h) {
            return new HistoriqueDto(h.getAncienStatut(), h.getNouveauStatut(), h.getAuteur(), h.getMotif(), h.getDate());
        }
    }

    public record CommentaireDto(String auteur, String contenu, boolean interne, LocalDateTime date) {
        public static CommentaireDto de(Commentaire c) {
            return new CommentaireDto(c.getAuteur(), c.getContenu(), c.isInterne(), c.getDate());
        }
    }

    public record ReclamationDto(Long id, String reference, String objet, String description, Canal canal,
                                 Categorie categorie, Categorie categorieSuggeree, Double confianceNlp,
                                 Priorite priorite, Statut statut, String client, String emailClient,
                                 String numeroCompte, String agent, LocalDateTime dateCreation,
                                 LocalDateTime dateEcheance, LocalDateTime dateCloture, boolean escaladee,
                                 boolean enRetard, Set<Statut> transitionsPossibles,
                                 List<HistoriqueDto> historique, List<CommentaireDto> commentaires) {}

    /** Vue publique pour le suivi par le client : sans commentaires internes ni données d'agent. */
    public record SuiviPublicDto(String reference, String objet, Statut statut, LocalDateTime dateCreation,
                                 LocalDateTime dateEcheance, List<HistoriqueDto> historique,
                                 List<CommentaireDto> reponses) {}

    public record StatistiquesDto(long total, long ouvertes, long enRetard, long escaladees,
                                  double tauxResolutionDansSla, double precisionNlp,
                                  java.util.Map<String, Long> parStatut, java.util.Map<String, Long> parCategorie) {}
}
