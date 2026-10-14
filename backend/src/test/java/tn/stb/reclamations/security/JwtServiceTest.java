package tn.stb.reclamations.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import tn.stb.reclamations.config.AppProperties;
import tn.stb.reclamations.model.Priorite;
import tn.stb.reclamations.model.Role;
import tn.stb.reclamations.model.Utilisateur;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService service(String secret) {
        return new JwtService(new AppProperties(new AppProperties.Jwt(secret, 60), new AppProperties.Nlp("x", 0.45),
                Map.of(Priorite.MOYENNE, 72), new AppProperties.Cors("x"), new AppProperties.Notifications(false)));
    }

    private final JwtService jwt = service("c2VjcmV0LWRlLXRlc3QtcG91ci1sZXMtdGVzdHMtdW5pdGFpcmVzLTIwMjY=");
    private final Utilisateur u = new Utilisateur("agent@stb.com.tn", "x", "Agent", Role.AGENT, Set.of());

    @Test
    void jetonGenere_contientLEmail() {
        assertEquals("agent@stb.com.tn", jwt.extraireEmail(jwt.generer(u)));
    }

    @Test
    void jetonModifie_estRejete() {
        String token = jwt.generer(u);
        String falsifie = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");
        assertThrows(JwtException.class, () -> jwt.extraireEmail(falsifie));
    }

    @Test
    void jetonSigneAvecUneAutreCle_estRejete() {
        JwtService autre = service("dW5lLWF1dHJlLWNsZS1zZWNyZXRlLXRvdXQtYS1mYWl0LWRpZmZlcmVudGUtMjAyNg==");
        assertThrows(JwtException.class, () -> jwt.extraireEmail(autre.generer(u)));
    }
}
