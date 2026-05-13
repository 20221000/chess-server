package com.chess.server.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 모든 커스텀 예외의 부모 클래스
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}