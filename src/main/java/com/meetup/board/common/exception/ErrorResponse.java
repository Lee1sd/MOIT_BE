package com.meetup.board.common.exception;

//예외가 터졌을 때 클라이언트(프론트)에게 실제로 내려주는 JSON 응답의 형태를 정의
public record ErrorResponse(String message) {
}
