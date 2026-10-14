package tn.stb.reclamations.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.stb.reclamations.repository.UtilisateurRepository;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UtilisateurRepository utilisateurs;

    public JwtFilter(JwtService jwt, UtilisateurRepository utilisateurs) {
        this.jwt = jwt;
        this.utilisateurs = utilisateurs;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String email = jwt.extraireEmail(header.substring(7));
                utilisateurs.findByEmail(email).filter(u -> u.isActif()).ifPresent(u -> {
                    var principal = new UtilisateurPrincipal(u);
                    var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (Exception ignored) {
                // jeton invalide ou expiré : la requête reste anonyme -> 401 sur les routes protégées
            }
        }
        chain.doFilter(req, res);
    }
}
