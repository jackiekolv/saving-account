package com.example.savingAccount.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseHttpException extends RuntimeException {

    private final HttpStatus status;

    public BaseHttpException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}