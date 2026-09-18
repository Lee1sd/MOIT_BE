package com.meetup.board.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 인증/계정
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."),

    // 토큰
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token 입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료되었거나 이미 사용된 refresh token 입니다."),

    // 모집글/신청
    CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "모집 정원이 마감되었습니다."),
    DUPLICATE_APPLICATION(HttpStatus.UNPROCESSABLE_ENTITY, "이미 신청한 모임입니다."),

    // 모집글/댓글/신청 - NotFound
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "모임 글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "신청 내역을 찾을 수 없습니다."),

    // 모집글/댓글/신청 - Forbidden
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "작성자만 삭제할 수 있습니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "작성자만 삭제할 수 있습니다."),
    COMMENT_UPDATE_DENIED(HttpStatus.FORBIDDEN, "본인 댓글만 수정할 수 있습니다."),
    APPLICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인 신청만 취소할 수 있습니다.");
    // TODO: StudyPostService/CommentService의 NotFoundException·ForbiddenException 확인 후 추가

    private final HttpStatus status;
    private final String message;
}