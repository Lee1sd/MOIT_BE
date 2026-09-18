package com.meetup.board.common.exception;

import org.springframework.http.HttpStatus;

// 정원이 가득 찼을 때 - 동시 신청 몰릴 때 부하테스트로 재현하는 대상 예외
public class CapacityExceededException extends BusinessException {
    public CapacityExceededException() {
        super("모집 정원이 마감되었습니다.", HttpStatus.CONFLICT);
    }
}
