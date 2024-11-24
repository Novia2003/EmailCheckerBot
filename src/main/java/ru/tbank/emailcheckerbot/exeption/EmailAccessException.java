package ru.tbank.emailcheckerbot.exeption;

public class EmailAccessException extends RuntimeException {
    public EmailAccessException(String message) {
        super(message);
    }
}
