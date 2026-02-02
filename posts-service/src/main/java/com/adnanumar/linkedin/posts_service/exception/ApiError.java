package com.adnanumar.linkedin.posts_service.exception;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiError {

    LocalDateTime timestamp;

    String error;

    HttpStatus statusCode;

    public ApiError() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(HttpStatus statusCode, String error) {
        this();
        this.statusCode = statusCode;
        this.error = error;
    }

}
