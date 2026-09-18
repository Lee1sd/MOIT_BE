package com.meetup.board.domain.application.dto;

import com.meetup.board.domain.application.ApplicationStatus;
import com.meetup.board.domain.application.StudyApplication;

public record ApplicationResponse(
        Long id,
        Long postId,
        ApplicationStatus status,
        String openChatUrl
) {
    public static ApplicationResponse from(StudyApplication application) {
        boolean approved = application.getStatus() == ApplicationStatus.APPLIED;
        return new ApplicationResponse(
                application.getId(),
                application.getPost().getId(),
                application.getStatus(),
                // 승인된 신청자에게만 오픈채팅 링크 노출 - 자체 채팅 구현 대신 채택한 방식
                approved ? application.getPost().getOpenChatUrl() : null
        );
    }
}
