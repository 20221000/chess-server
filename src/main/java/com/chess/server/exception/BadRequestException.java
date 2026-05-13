package com.chess.server.exception;

import org.springframework.http.HttpStatus;

// 잘못된 요청 → 400
public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}