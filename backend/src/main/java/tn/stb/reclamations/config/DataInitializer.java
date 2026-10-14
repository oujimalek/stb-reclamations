package tn.stb.reclamations.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.stb.reclamations.model.Categorie;
import tn.stb.reclamations.model.Role;
import tn.stb.reclamations.model.Utilisateur;
import tn.stb.reclamations.repository.UtilisateurRepository;

import java.util.EnumSet;
import java.util.Set;

/** Comptes de démonstration (mot de passe : Stb@2026). */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUtilisateurs(UtilisateurRepository repo, PasswordEncoder enc) {
        return args -> {
            if (repo.count() > 0) return;
            String mdp = enc.encode("Stb@2026");
            repo.save(new Utilisateur("admin@stb.com.tn", mdp, "Administrateur", Role.ADMIN, Set.of()));
            repo.save(new Utilisateur("superviseur@stb.com.tn", mdp, "Sonia Ben Salah", Role.SUPERVISEUR, Set.of()));
            repo.save(new Utilisateur("agent.monetique@stb.com.tn", mdp, "Karim Trabelsi", Role.AGENT,
                    EnumSet.of(Categorie.CARTE_BANCAIRE, Categorie.BANQUE_DIGITALE)));
            repo.save(new Utilisateur("agent.operations@stb.com.tn", mdp, "Ines Mansouri", Role.AGENT,
                    EnumSet.of(Categorie.VIREMENT, Categorie.CHEQUE, Categorie.COMPTE)));
            repo.save(new Utilisateur("agent.credit@stb.com.tn", mdp, "Youssef Gharbi", Role.AGENT,
                    EnumSet.of(Categorie.CREDIT, Categorie.FRAIS_COMMISSIONS)));
            repo.save(new Utilisateur("agent.relation@stb.com.tn", mdp, "Amira Jlassi", Role.AGENT,
                    EnumSet.of(Categorie.ACCUEIL_AGENCE, Categorie.AUTRE, Categorie.COMPTE)));
        };
    }
}
