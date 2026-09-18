package com.meetup.board.domain.post.dto;

import com.meetup.board.domain.post.PostCategory;
import com.meetup.board.domain.post.PostStatus;
import com.meetup.board.domain.post.StudyPost;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        PostCategory category,
        int capacity,
        int currentCount,
        PostStatus status,
        LocalDateTime deadline,
        String authorNickname,
        LocalDateTime createdAt
) {
    public static PostResponse from(StudyPost post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getCapacity(),
                post.getCurrentCount(),
                post.getStatus(),
                post.getDeadline(),
                post.getAuthor().getNickname(),
                post.getCreatedAt()
        );
    }
}
