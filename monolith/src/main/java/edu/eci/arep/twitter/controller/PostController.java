package edu.eci.arep.twitter.controller;

import edu.eci.arep.twitter.dto.CreatePostRequest;
import edu.eci.arep.twitter.dto.ErrorResponse;
import edu.eci.arep.twitter.model.Post;
import edu.eci.arep.twitter.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/api/posts")
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @PostMapping("/api/posts")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Post> createPost(
            @Valid @RequestBody CreatePostRequest request,
            JwtAuthenticationToken token) {
        Map<String, Object> claims = token.getTokenAttributes();
        String authorSub = (String) claims.getOrDefault("sub", "");
        String authorName = (String) claims.getOrDefault("name", "");
        Post post = postService.createPost(request.content(), authorSub, authorName);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .findFirst()
            .orElse("Validation failed");
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, "Bad Request", message));
    }
}
