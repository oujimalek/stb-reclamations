package tn.stb.reclamations.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.stb.reclamations.config.AppProperties;
import tn.stb.reclamations.model.Reclamation;

/** Notifie le client (accusé de réception, changement de statut) et le superviseur (escalade). */
@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender mail;
    private final boolean actif;

    public NotificationService(JavaMailSender mail, AppProperties props) {
        this.mail = mail;
        this.actif = props.notifications() != null && props.notifications().emailActif();
    }

    public void notifierClient(Reclamation r, String sujet, String message) {
        envoyer(r.getClient().getEmail(), "[STB] " + sujet + " — " + r.getReference(), message);
    }

    public void notifierEscalade(Reclamation r, String emailSuperviseur) {
        envoyer(emailSuperviseur, "[STB] Escalade " + r.getReference(),
                "La réclamation " + r.getReference() + " a dépassé son délai de traitement (SLA).");
    }

    private void envoyer(String to, String sujet, String texte) {
        log.info("Notification -> {} : {}", to, sujet);
        if (!actif) return;
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setTo(to);
            m.setSubject(sujet);
            m.setText(texte);
            mail.send(m);
        } catch (Exception e) {
            log.warn("Échec d'envoi de l'e-mail : {}", e.getMessage());
        }
    }
}
