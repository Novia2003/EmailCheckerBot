package ru.tbank.emailcheckerbot.bot.command;

import lombok.Getter;

@Getter
public enum Command {
    START("/start"),
    ADD_EMAIL("/add_email"),
    COMMANDS("/commands"),
    EMAILS_LIST("/emails");

    private final String title;

    Command(String title) {
        this.title = title;
    }
}
