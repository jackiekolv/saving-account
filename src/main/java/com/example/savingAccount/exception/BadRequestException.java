package com.example.savingAccount.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseHttpException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}