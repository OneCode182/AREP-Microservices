package edu.eci.arep.twitter;

import edu.eci.arep.twitter.model.Post;
import edu.eci.arep.twitter.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldReturnStreamWithoutAuth() throws Exception {
        when(postService.getAllPosts()).thenReturn(List.of());
        mockMvc.perform(get("/api/stream"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldRejectPostWithoutJwt() throws Exception {
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Hello world\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectPostOver140Chars() throws Exception {
        String longContent = "a".repeat(141);
        mockMvc.perform(post("/api/posts")
                .with(jwt().jwt(builder -> builder
                    .claim("sub", "auth0|123")
                    .claim("name", "Test User")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"" + longContent + "\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreatePostWithValidJwt() throws Exception {
        Post mockPost = new Post("Hello world", "auth0|123", "Test User");
        mockPost.setId(1L);
        mockPost.setCreatedAt(Instant.now());

        when(postService.createPost(any(), any(), any())).thenReturn(mockPost);

        mockMvc.perform(post("/api/posts")
                .with(jwt().jwt(builder -> builder
                    .claim("sub", "auth0|123")
                    .claim("name", "Test User")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Hello world\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("Hello world"));
    }

    @Test
    void shouldReturnUserInfoWithJwt() throws Exception {
        mockMvc.perform(get("/api/me")
                .with(jwt().jwt(builder -> builder
                    .claim("sub", "auth0|123")
                    .claim("email", "test@example.com")
                    .claim("name", "Test User"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sub").value("auth0|123"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void postServiceCreatesPost() {
        Post post = new Post("Test content", "auth0|456", "Another User");
        assert post.getContent().equals("Test content");
        assert post.getAuthorSub().equals("auth0|456");
        assert post.getAuthorName().equals("Another User");
    }
}
