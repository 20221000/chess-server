package com.chess.server.exception;

import org.springframework.http.HttpStatus;

// 중복 데이터 존재할 때 → 409
public class DuplicateException extends BusinessException {

    public DuplicateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}