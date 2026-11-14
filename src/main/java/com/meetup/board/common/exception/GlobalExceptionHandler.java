package com.meetup.board.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getMessage()));
    }

    private static final String APPLICATION_UNIQUE_CONSTRAINT = "uk_app_post_user";

    // DB unique 제약(post_id, applicant_id)에 걸린 동시 요청의 마지막 방어선.
    // 사전 중복 검사(StudyApplicationService.apply)와 같은 DUPLICATE_APPLICATION 응답으로 통일한다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException e) {
        log.warn("data integrity violation: {}", e.getMessage());
        if (isApplicationDuplicate(e)) {
            ErrorCode code = ErrorCode.DUPLICATE_APPLICATION;
            return ResponseEntity.status(code.getStatus()).body(new ErrorResponse(code.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("이미 처리된 요청입니다."));
    }

    private boolean isApplicationDuplicate(DataIntegrityViolationException e) {
        if (e.getCause() instanceof ConstraintViolationException cve
                && cve.getConstraintName() != null
                && cve.getConstraintName().contains(APPLICATION_UNIQUE_CONSTRAINT)) {
            return true;
        }
        String message = e.getMostSpecificCause().getMessage();
        return message != null && message.contains(APPLICATION_UNIQUE_CONSTRAINT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("unexpected error", e);
        return ResponseEntity.internalServerError().body(new ErrorResponse("서버 오류가 발생했습니다."));
    }
}
