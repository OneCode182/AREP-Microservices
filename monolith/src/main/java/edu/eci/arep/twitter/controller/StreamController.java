package edu.eci.arep.twitter.controller;

import edu.eci.arep.twitter.model.Post;
import edu.eci.arep.twitter.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Stream", description = "Endpoints for the public live feed")
public class StreamController {

    private final PostService postService;

    public StreamController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/api/stream")
    @Operation(summary = "Get the public stream", description = "Retrieves the main feed of all posts. Currently identical to /api/posts.")
    @ApiResponse(responseCode = "200", description = "Stream retrieved successfully")
    public ResponseEntity<List<Post>> getStream() {
        return ResponseEntity.ok(postService.getAllPosts());
    }
}
