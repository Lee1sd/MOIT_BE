package com.meetup.board.domain.post.dto;

import com.meetup.board.domain.post.PostCategory;
import com.meetup.board.domain.post.PostStatus;
import com.meetup.board.domain.post.StudyPost;

public record PostSummaryResponse(
        Long id,
        String title,
        PostCategory category,
        int capacity,
        int currentCount,
        PostStatus status
) {
    public static PostSummaryResponse from(StudyPost post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getCapacity(),
                post.getCurrentCount(),
                post.getStatus()
        );
    }
}
