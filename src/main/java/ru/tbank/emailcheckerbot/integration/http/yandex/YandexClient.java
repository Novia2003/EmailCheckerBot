package ru.tbank.emailcheckerbot.integration.http.yandex;

import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.yandex.YandexUserInfoDTO;

public interface YandexClient {
    YandexUserInfoDTO getUserInfo(String token);

    AccessTokenResponseDTO getAccessToken(String code);

    RefreshTokenResponseDTO refreshAccessToken(String refreshToken);
}
