package com.meetup.board.common.exception;

//예외가 터졌을 때 클라이언트(프론트)에게 실제로 내려주는 JSON 응답의 형태를 정의
// code: ErrorCode enum의 name() (예: DUPLICATE_APPLICATION), message: 사용자에게 보여줄 메시지
public record ErrorResponse(String code, String message) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage());
    }
}
