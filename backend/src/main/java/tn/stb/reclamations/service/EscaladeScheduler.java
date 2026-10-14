package tn.stb.reclamations.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.stb.reclamations.model.Reclamation;
import tn.stb.reclamations.model.Role;
import tn.stb.reclamations.model.Statut;
import tn.stb.reclamations.repository.ReclamationRepository;
import tn.stb.reclamations.repository.UtilisateurRepository;

import java.time.LocalDateTime;
import java.util.EnumSet;

/** Tâche planifiée : escalade automatique des réclamations dont l'échéance SLA est dépassée. */
@Component
public class EscaladeScheduler {
    private static final Logger log = LoggerFactory.getLogger(EscaladeScheduler.class);
    private static final EnumSet<Statut> NON_CONCERNEES =
            EnumSet.of(Statut.RESOLUE, Statut.CLOTUREE, Statut.REJETEE, Statut.ESCALADEE, Statut.EN_ATTENTE_CLIENT);

    private final ReclamationRepository reclamations;
    private final UtilisateurRepository utilisateurs;
    private final ReclamationService service;
    private final NotificationService notifications;

    public EscaladeScheduler(ReclamationRepository reclamations, UtilisateurRepository utilisateurs,
                             ReclamationService service, NotificationService notifications) {
        this.reclamations = reclamations; this.utilisateurs = utilisateurs;
        this.service = service; this.notifications = notifications;
    }

    @Scheduled(fixedDelayString = "PT15M", initialDelayString = "PT1M")
    @Transactional
    public void escalader() {
        var enRetard = reclamations.findByStatutNotInAndEscaladeeFalseAndDateEcheanceBefore(NON_CONCERNEES, LocalDateTime.now());
        var superviseurs = utilisateurs.findByRoleAndActifTrue(Role.SUPERVISEUR);
        for (Reclamation r : enRetard) {
            if (r.getStatut() == Statut.EN_COURS) {
                service.changerStatutInterne(r, Statut.ESCALADEE, "système (SLA)", "Délai de traitement dépassé");
            } else {
                r.setEscaladee(true); // non encore prise en charge : on marque et on alerte
            }
            superviseurs.forEach(s -> notifications.notifierEscalade(r, s.getEmail()));
        }
        if (!enRetard.isEmpty()) log.info("{} réclamation(s) escaladée(s)", enRetard.size());
    }
}
