package ru.pionerpixel.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException() {
        super("Unauthorized access to user data");
    }
}