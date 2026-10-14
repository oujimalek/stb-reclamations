package tn.stb.reclamations.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import tn.stb.reclamations.model.Priorite;

import java.util.Map;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Nlp nlp, Map<Priorite, Integer> slaHeures, Cors cors, Notifications notifications) {
    public record Jwt(String secret, long expirationMinutes) {}
    public record Nlp(String url, double seuilConfiance) {}
    public record Cors(String origins) {}
    public record Notifications(boolean emailActif) {}
}
