package tn.stb.reclamations.web;

import tn.stb.reclamations.model.Reclamation;
import tn.stb.reclamations.web.dto.Dtos.*;
import tn.stb.reclamations.workflow.WorkflowEngine;

final class Mapper {
    private Mapper() {}

    static ReclamationDto dto(Reclamation r, WorkflowEngine wf) {
        return new ReclamationDto(r.getId(), r.getReference(), r.getObjet(), r.getDescription(), r.getCanal(),
                r.getCategorie(), r.getCategorieSuggeree(), r.getConfianceNlp(), r.getPriorite(), r.getStatut(),
                r.getClient().getNomComplet(), r.getClient().getEmail(), r.getClient().getNumeroCompte(),
                r.getAgent() == null ? null : r.getAgent().getNomComplet(),
                r.getDateCreation(), r.getDateEcheance(), r.getDateCloture(), r.isEscaladee(), r.estEnRetard(),
                wf.transitionsPossibles(r.getStatut()),
                r.getHistorique().stream().map(HistoriqueDto::de).toList(),
                r.getCommentaires().stream().map(CommentaireDto::de).toList());
    }

    static SuiviPublicDto suivi(Reclamation r) {
        return new SuiviPublicDto(r.getReference(), r.getObjet(), r.getStatut(), r.getDateCreation(),
                r.getDateEcheance(), r.getHistorique().stream().map(HistoriqueDto::de).toList(),
                r.getCommentaires().stream().filter(c -> !c.isInterne()).map(CommentaireDto::de).toList());
    }
}
