package com.example.savingAccount.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseHttpException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}