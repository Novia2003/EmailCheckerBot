package ru.tbank.emailcheckerbot.exeption;

public class InvalidCallbackQueryException extends RuntimeException {
    public InvalidCallbackQueryException(String message) {
        super(message);
    }
}
