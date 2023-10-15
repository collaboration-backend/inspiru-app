package com.stc.inspireu.exceptions;


import org.springframework.http.HttpStatus;

import java.util.Objects;

public class CustomRunTimeException extends RuntimeException {

    private String message;

    private HttpStatus httpStatus;

    public CustomRunTimeException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public CustomRunTimeException(String message) {
        this.message = message;
    }

    private CustomRunTimeException() {
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return this.message;
    }

    public HttpStatus getHttpStatus() {
        return Objects.nonNull(httpStatus) ? httpStatus : HttpStatus.BAD_REQUEST;
    }
}
