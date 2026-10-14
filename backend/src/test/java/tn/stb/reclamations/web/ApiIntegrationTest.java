package tn.stb.reclamations.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Tests d'intégration de l'API (base H2, service NLP absent : repli sur la qualification manuelle). */
@SpringBootTest(properties = "app.nlp.url=http://localhost:1")
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String token(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"motDePasse\":\"Stb@2026\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(body).get("token").asText();
    }

    @Test
    void loginAvecMauvaisMotDePasse_401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"agent.credit@stb.com.tn\",\"motDePasse\":\"faux\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void routeProtegeeSansJeton_401() throws Exception {
        mvc.perform(get("/api/reclamations")).andExpect(status().isUnauthorized());
    }

    @Test
    void statistiquesInterditesAUnAgent_403() throws Exception {
        mvc.perform(get("/api/statistiques").header("Authorization", token("agent.credit@stb.com.tn")))
                .andExpect(status().isForbidden());
    }

    @Test
    void parcoursComplet_depotSuiviEtTransitions() throws Exception {
        // 1. dépôt public
        String depot = mvc.perform(post("/api/public/reclamations").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nomClient":"Test Client","emailClient":"client.test@mail.tn",
                                 "objet":"Virement non reçu","description":"Mon virement n'est jamais arrivé chez le bénéficiaire"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").exists())
                .andExpect(jsonPath("$.statut").value("NOUVELLE"))
                .andReturn().getResponse().getContentAsString();
        String ref = json.readTree(depot).get("reference").asText();

        // 2. suivi public : mauvais e-mail refusé, bon e-mail accepté
        mvc.perform(get("/api/public/reclamations/" + ref).param("email", "autre@mail.tn")).andExpect(status().isNotFound());
        mvc.perform(get("/api/public/reclamations/" + ref).param("email", "client.test@mail.tn")).andExpect(status().isOk());

        // 3. le superviseur retrouve la réclamation
        String sup = token("superviseur@stb.com.tn");
        JsonNode liste = json.readTree(mvc.perform(get("/api/reclamations").header("Authorization", sup))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        long id = -1;
        for (JsonNode n : liste) if (n.get("reference").asText().equals(ref)) id = n.get("id").asLong();

        // 4. transition interdite -> 409
        mvc.perform(patch("/api/reclamations/" + id + "/statut").header("Authorization", sup)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"statut\":\"CLOTUREE\"}"))
                .andExpect(status().isConflict());

        // 5. qualification manuelle -> affectation automatique à l'agent habilité
        mvc.perform(patch("/api/reclamations/" + id + "/qualification").header("Authorization", sup)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"categorie\":\"VIREMENT\",\"priorite\":\"HAUTE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("AFFECTEE"))
                .andExpect(jsonPath("$.agent").value("Ines Mansouri"));
    }
}
