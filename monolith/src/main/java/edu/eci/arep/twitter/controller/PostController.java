package edu.eci.arep.twitter.controller;

import edu.eci.arep.twitter.dto.CreatePostRequest;
import edu.eci.arep.twitter.dto.ErrorResponse;
import edu.eci.arep.twitter.model.Post;
import edu.eci.arep.twitter.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Posts", description = "Endpoints for creating and retrieving short posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/api/posts")
    @Operation(summary = "Get all posts", description = "Retrieves a complete list of all public posts from the database.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved posts", 
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)))
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @PostMapping("/api/posts")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new post", description = "Allows an authenticated user to create a post of up to 140 characters. Author information is extracted from the JWT token.")
    @ApiResponse(responseCode = "201", description = "Post created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body (e.g., content too long)", 
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
    public ResponseEntity<Post> createPost(
            @Valid @RequestBody CreatePostRequest request,
            JwtAuthenticationToken token) {
        Map<String, Object> claims = token.getTokenAttributes();
        String authorSub = (String) claims.getOrDefault("sub", "");
        String jwtName = (String) claims.getOrDefault("name", "");
        String authorName = (request.authorName() != null && !request.authorName().isBlank())
                ? request.authorName()
                : jwtName;
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
