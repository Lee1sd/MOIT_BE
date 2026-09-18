package com.meetup.board.domain.post.dto;

import com.meetup.board.domain.post.PostCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PostCreateRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull PostCategory category,
        @Min(2) int capacity,
        @NotNull @Future LocalDateTime deadline,
        String openChatUrl
) {}
