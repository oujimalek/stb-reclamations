package tn.stb.reclamations.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import tn.stb.reclamations.config.AppProperties;
import tn.stb.reclamations.model.Utilisateur;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey cle;
    private final long expirationMinutes;

    public JwtService(AppProperties props) {
        this.cle = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.jwt().secret()));
        this.expirationMinutes = props.jwt().expirationMinutes();
    }

    public String generer(Utilisateur u) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(u.getEmail())
                .claim("role", u.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(cle)
                .compact();
    }

    /** Retourne l'e-mail (sujet) si le jeton est valide, sinon lève une exception JwtException. */
    public String extraireEmail(String token) {
        Claims claims = Jwts.parser().verifyWith(cle).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }
}
