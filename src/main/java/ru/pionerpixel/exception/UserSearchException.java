package ru.pionerpixel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserSearchException extends RuntimeException {
    public UserSearchException(String message) {
        super(message);
    }
}
