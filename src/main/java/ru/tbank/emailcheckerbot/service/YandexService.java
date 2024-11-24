package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ru.tbank.emailcheckerbot.entity.MailProvider;
import ru.tbank.emailcheckerbot.configuration.property.YandexProperties;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.integration.http.yandex.YandexClient;

@Service
@RequiredArgsConstructor
public class YandexService {

    private static final String AUTHORIZE_URL = "/authorize";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String STATE_PARAM = "state";
    private static final String RESPONSE_TYPE_VALUE = "code";

    private final YandexProperties yandexProperties;

    private final YandexClient yandexClient;

    public String getAuthUrl(Long userId) {
        String state = userId + " " + MailProvider.YANDEX;

        return UriComponentsBuilder.fromHttpUrl(yandexProperties.getAuthUrl() + AUTHORIZE_URL)
                .queryParam(RESPONSE_TYPE_PARAM, RESPONSE_TYPE_VALUE)
                .queryParam(CLIENT_ID_PARAM, yandexProperties.getClientId())
                .queryParam(STATE_PARAM, state)
                .build()
                .toUriString();
    }

    public String getEmail(String token) {
        return yandexClient.getUserInfo(token).getDefaultEmail();
    }

    public String getSettingsUrl() {
        return yandexProperties.getSettingsUrl();
    }

    public AccessTokenResponseDTO getAccessTokenResponse(String code) {
        return yandexClient.getAccessToken(code);
    }

    public RefreshTokenResponseDTO getRefreshTokenResponse(String refreshToken) {
        return yandexClient.refreshAccessToken(refreshToken);
    }
}
