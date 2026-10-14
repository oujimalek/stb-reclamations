package tn.stb.reclamations.web;

import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import tn.stb.reclamations.security.JwtService;
import tn.stb.reclamations.security.UtilisateurPrincipal;
import tn.stb.reclamations.web.dto.Dtos.LoginRequest;
import tn.stb.reclamations.web.dto.Dtos.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthController(AuthenticationManager authManager, JwtService jwt) {
        this.authManager = authManager;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        var auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.motDePasse()));
        var u = ((UtilisateurPrincipal) auth.getPrincipal()).utilisateur();
        return new LoginResponse(jwt.generer(u), u.getNomComplet(), u.getEmail(), u.getRole());
    }
}
