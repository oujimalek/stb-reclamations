package tn.stb.reclamations.nlp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tn.stb.reclamations.config.AppProperties;

import java.util.Map;
import java.util.Optional;

/** Client HTTP du micro-service Python de classification. */
@Component
public class NlpClient {
    private static final Logger log = LoggerFactory.getLogger(NlpClient.class);
    private final RestClient rest;

    public record Prediction(String categorie, double confianceCategorie, String priorite,
                             double confiancePriorite, boolean qualificationManuelle,
                             Map<String, Double> alternatives) {}

    public NlpClient(AppProperties props) {
        this.rest = RestClient.builder().baseUrl(props.nlp().url()).build();
    }

    /** Retourne vide si le service NLP est indisponible : la réclamation sera qualifiée manuellement. */
    public Optional<Prediction> classer(String texte) {
        try {
            Prediction p = rest.post().uri("/api/nlp/classify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("texte", texte))
                    .retrieve()
                    .body(Prediction.class);
            return Optional.ofNullable(p);
        } catch (Exception e) {
            log.warn("Service NLP indisponible : {}", e.getMessage());
            return Optional.empty();
        }
    }
}
