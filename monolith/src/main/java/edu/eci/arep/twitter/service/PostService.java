package edu.eci.arep.twitter.service;

import edu.eci.arep.twitter.model.Post;
import edu.eci.arep.twitter.repository.PostRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(String content, String authorSub, String authorName) {
        Post post = new Post(content, authorSub, authorName);
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }
}
