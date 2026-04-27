package edu.eci.arep.twitter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "User", description = "Endpoints for user profile information")
public class UserController {

    @GetMapping("/api/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user profile", description = "Extracts and returns user information (name, email, sub) from the Auth0 JWT token claims.")
    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
    public ResponseEntity<Map<String, Object>> me(JwtAuthenticationToken token) {
        Map<String, Object> claims = token.getTokenAttributes();
        return ResponseEntity.ok(Map.of(
            "sub", claims.getOrDefault("sub", ""),
            "email", claims.getOrDefault("email", ""),
            "name", claims.getOrDefault("name", "")
        ));
    }
}
