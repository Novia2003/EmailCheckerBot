package ru.tbank.emailcheckerbot.exeption;

public class UserEmailRedisEntityNotFoundException extends RuntimeException {
    public UserEmailRedisEntityNotFoundException(String message) {
        super(message);
    }
}
