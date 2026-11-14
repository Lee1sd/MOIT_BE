package com.meetup.board.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String APPLICATION_UNIQUE_CONSTRAINT = "uk_app_post_user";
    private static final Pattern FOR_KEY_PATTERN = Pattern.compile("for key '([^']+)'");

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        return toResponse(e.getErrorCode());
    }

    // DB unique 제약(post_id, applicant_id)에 걸린 동시 요청의 마지막 방어선.
    // 사전 중복 검사(StudyApplicationService.apply)와 같은 DUPLICATE_APPLICATION 응답으로 통일한다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException e) {
        log.warn("data integrity violation: {}", e.getMessage());
        if (isApplicationDuplicate(e)) {
            return toResponse(ErrorCode.DUPLICATE_APPLICATION);
        }
        return toResponse(ErrorCode.DATA_INTEGRITY_VIOLATION);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("unexpected error", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode));
    }

    /**
     * 위반된 제약이 신청 중복(uk_app_post_user)인지 판정한다.
     * 1) Hibernate가 제약 이름을 줬으면 그 이름으로만 판정한다 (메시지는 보지 않음).
     * 2) 제약 이름이 없을 때만 DB 메시지의 "for key '...'" 부분에서 제약 이름을 추출한다.
     * 메시지 전체에서 문자열을 찾으면 중복된 값 자체(예: 'uk_app_post_user@example.com')에
     * 걸려 오판하므로 금지.
     */
    private boolean isApplicationDuplicate(DataIntegrityViolationException e) {
        String constraintName = findHibernateConstraintName(e);
        if (constraintName == null) {
            constraintName = extractConstraintNameFromMessage(e.getMostSpecificCause().getMessage());
        }
        return APPLICATION_UNIQUE_CONSTRAINT.equalsIgnoreCase(stripTablePrefix(constraintName));
    }

    private String findHibernateConstraintName(Throwable e) {
        for (Throwable t = e; t != null && t.getCause() != t; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve
                    && cve.getConstraintName() != null
                    && !cve.getConstraintName().isBlank()) {
                return cve.getConstraintName();
            }
        }
        return null;
    }

    // MySQL: "Duplicate entry '<값>' for key '<제약>'".
    // 값 안에 "for key '...'"가 들어 있어도 실제 제약 이름은 메시지 끝에 오므로 마지막 매치를 쓴다.
    private String extractConstraintNameFromMessage(String message) {
        if (message == null) {
            return null;
        }
        Matcher matcher = FOR_KEY_PATTERN.matcher(message);
        String last = null;
        while (matcher.find()) {
            last = matcher.group(1);
        }
        return last;
    }

    // MySQL 8은 'table.constraint' 형식이라 테이블 접두사를 떼고 제약 이름만 비교한다.
    private String stripTablePrefix(String constraintName) {
        if (constraintName == null) {
            return null;
        }
        int dot = constraintName.lastIndexOf('.');
        return dot >= 0 ? constraintName.substring(dot + 1) : constraintName;
    }
}
