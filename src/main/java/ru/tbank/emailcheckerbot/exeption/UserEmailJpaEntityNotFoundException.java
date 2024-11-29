package ru.tbank.emailcheckerbot.exeption;

public class UserEmailJpaEntityNotFoundException extends RuntimeException {
    public UserEmailJpaEntityNotFoundException(String message) {
        super(message);
    }
}
