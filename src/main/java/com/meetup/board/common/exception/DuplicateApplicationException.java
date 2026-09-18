package com.meetup.board.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateApplicationException extends BusinessException {
    public DuplicateApplicationException() {
        super("이미 신청한 모임입니다.", HttpStatus.CONFLICT);
    }
}
