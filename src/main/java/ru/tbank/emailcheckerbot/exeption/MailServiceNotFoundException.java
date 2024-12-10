package ru.tbank.emailcheckerbot.exeption;

public class MailServiceNotFoundException extends RuntimeException {
    public MailServiceNotFoundException(String message) {
        super(message);
    }
}
