package com.example.savingAccount.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseHttpException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}