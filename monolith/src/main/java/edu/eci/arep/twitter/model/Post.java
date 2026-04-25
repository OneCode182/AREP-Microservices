package edu.eci.arep.twitter.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    @Size(max = 140, message = "Post content must not exceed 140 characters")
    @NotBlank
    private String content;

    @Column(nullable = false)
    private String authorSub;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Post() {}

    public Post(String content, String authorSub, String authorName) {
        this.content = content;
        this.authorSub = authorSub;
        this.authorName = authorName;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorSub() { return authorSub; }
    public void setAuthorSub(String authorSub) { this.authorSub = authorSub; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
