package com.chess.server.exception;

import org.springframework.http.HttpStatus;

// 리소스를 찾을 수 없을 때 → 404
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}