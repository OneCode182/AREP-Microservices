package edu.eci.arep.twitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
    @NotBlank(message = "Content must not be blank")
    @Size(max = 140, message = "Post content must not exceed 140 characters")
    String content,
    String authorName
) {}
