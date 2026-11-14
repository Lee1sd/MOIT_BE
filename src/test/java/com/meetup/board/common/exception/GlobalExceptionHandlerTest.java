package com.meetup.board.common.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 사전_중복검사_예외는_409_DUPLICATE_APPLICATION() {
        ResponseEntity<ErrorResponse> res =
                handler.handleBusiness(new BusinessException(ErrorCode.DUPLICATE_APPLICATION));

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody().message()).isEqualTo("이미 신청한 모임입니다.");
    }

    @Test
    void 신청_유니크제약_위반은_사전_중복검사와_같은_응답() {
        SQLIntegrityConstraintViolationException sql = new SQLIntegrityConstraintViolationException(
                "Duplicate entry '1-2' for key 'applications.uk_app_post_user'");
        DataIntegrityViolationException e = new DataIntegrityViolationException(
                "could not execute statement",
                new ConstraintViolationException("could not execute statement", sql, "applications.uk_app_post_user"));

        ResponseEntity<ErrorResponse> integrity = handler.handleIntegrity(e);
        ResponseEntity<ErrorResponse> business =
                handler.handleBusiness(new BusinessException(ErrorCode.DUPLICATE_APPLICATION));

        assertThat(integrity.getStatusCode()).isEqualTo(business.getStatusCode());
        assertThat(integrity.getBody()).isEqualTo(business.getBody());
    }

    @Test
    void 제약명을_못_뽑아도_DB_메시지로_신청_중복을_판별() {
        DataIntegrityViolationException e = new DataIntegrityViolationException(
                "could not execute statement",
                new SQLIntegrityConstraintViolationException(
                        "Duplicate entry '1-2' for key 'applications.uk_app_post_user'"));

        ResponseEntity<ErrorResponse> res = handler.handleIntegrity(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody().message()).isEqualTo(ErrorCode.DUPLICATE_APPLICATION.getMessage());
    }

    @Test
    void 다른_제약_위반은_일반_409_응답() {
        DataIntegrityViolationException e = new DataIntegrityViolationException(
                "could not execute statement",
                new SQLIntegrityConstraintViolationException(
                        "Duplicate entry 'a@b.com' for key 'users.uk_users_email'"));

        ResponseEntity<ErrorResponse> res = handler.handleIntegrity(e);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody().message()).isEqualTo("이미 처리된 요청입니다.");
    }
}
