package ru.tbank.emailcheckerbot.exeption;

public class EncryptionException extends RuntimeException {
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}