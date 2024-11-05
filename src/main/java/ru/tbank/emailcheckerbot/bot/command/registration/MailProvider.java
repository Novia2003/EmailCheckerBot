package ru.tbank.emailcheckerbot.bot.command.registration;

import lombok.Getter;

@Getter
public enum MailProvider {
    YANDEX("Yandex", "yandex"),
    GOOGLE("Google", "gmail"),
    MAILRu("Mail.ru", "mailru");

    private final String title;
    private final String configurationName;

    MailProvider(String title, String configurationName) {
        this.title = title;
        this.configurationName = configurationName;
    }
}

