package ru.tbank.emailcheckerbot.entity;

import lombok.Getter;

@Getter
public enum MailProvider {
    YANDEX("Yandex", "yandex"),
    MAILRu("Mail.ru", "mailru");

    private final String title;
    private final String configurationName;

    MailProvider(String title, String configurationName) {
        this.title = title;
        this.configurationName = configurationName;
    }
}

