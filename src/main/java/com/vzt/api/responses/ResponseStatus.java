package com.vzt.api.responses;

import org.springframework.http.HttpStatus;

public enum ResponseStatus {
    SUCCESS(HttpStatus.OK),
    ERROR(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),;

    public final HttpStatus value;

    private ResponseStatus(HttpStatus value) {
        this.value = value;
    }
}
