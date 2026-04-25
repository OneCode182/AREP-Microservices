package edu.eci.arep.twitter.controller;

import edu.eci.arep.twitter.model.Post;
import edu.eci.arep.twitter.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StreamController {

    private final PostService postService;

    public StreamController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/api/stream")
    public ResponseEntity<List<Post>> getStream() {
        return ResponseEntity.ok(postService.getAllPosts());
    }
}
