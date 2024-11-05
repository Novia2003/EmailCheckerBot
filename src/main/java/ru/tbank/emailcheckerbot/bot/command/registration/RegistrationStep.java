package ru.tbank.emailcheckerbot.bot.command.registration;

public enum RegistrationStep {
    NONE,
    CHOOSING_PROVIDER,
    WAITING_FOR_TOKEN,
    WAITING_FOR_PERMISSION_CONFIRMATION
}
