package ru.tbank.emailcheckerbot.integration.http;

import ru.tbank.emailcheckerbot.dto.yandex.YandexUserInfoDTO;

public interface YandexClient {
    YandexUserInfoDTO getUserInfo(String token);
}
