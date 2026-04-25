package edu.eci.arep.twitter.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/api/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> me(JwtAuthenticationToken token) {
        Map<String, Object> claims = token.getTokenAttributes();
        return ResponseEntity.ok(Map.of(
            "sub", claims.getOrDefault("sub", ""),
            "email", claims.getOrDefault("email", ""),
            "name", claims.getOrDefault("name", "")
        ));
    }
}
