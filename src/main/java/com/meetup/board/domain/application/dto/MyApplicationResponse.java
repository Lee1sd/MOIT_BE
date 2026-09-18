package com.meetup.board.domain.application.dto;

import com.meetup.board.domain.application.ApplicationStatus;
import com.meetup.board.domain.application.StudyApplication;
import com.meetup.board.domain.post.PostCategory;

import java.time.LocalDateTime;

// 마이페이지 "내가 신청한 모임" 목록용 - 신청 상태 + 모임 정보를 한 화면에서 보여주기 위해 함께 담음
public record MyApplicationResponse(
        Long applicationId,
        Long postId,
        String postTitle,
        PostCategory category,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {
    public static MyApplicationResponse from(StudyApplication application) {
        return new MyApplicationResponse(
                application.getId(),
                application.getPost().getId(),
                application.getPost().getTitle(),
                application.getPost().getCategory(),
                application.getStatus(),
                application.getAppliedAt()
        );
    }
}
