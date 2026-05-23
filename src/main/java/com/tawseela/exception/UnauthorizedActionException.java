package com.tawseela.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedActionException extends BusinessException {

    public UnauthorizedActionException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
