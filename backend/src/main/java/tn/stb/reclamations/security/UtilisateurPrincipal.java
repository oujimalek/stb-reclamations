package tn.stb.reclamations.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.stb.reclamations.model.Utilisateur;

import java.util.Collection;
import java.util.List;

public record UtilisateurPrincipal(Utilisateur utilisateur) implements UserDetails {
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()));
    }
    @Override public String getPassword() { return utilisateur.getMotDePasse(); }
    @Override public String getUsername() { return utilisateur.getEmail(); }
    @Override public boolean isEnabled() { return utilisateur.isActif(); }
}
