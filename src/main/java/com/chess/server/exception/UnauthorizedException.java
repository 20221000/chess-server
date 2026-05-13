package com.chess.server.exception;

import org.springframework.http.HttpStatus;

// 인증 실패할 때 → 401
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}