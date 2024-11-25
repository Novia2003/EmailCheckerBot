package ru.tbank.emailcheckerbot.service.provider.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.configuration.property.provider.YandexProperties;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.integration.http.yandex.YandexClient;
import ru.tbank.emailcheckerbot.service.provider.MailService;

@Service
@RequiredArgsConstructor
public class YandexService implements MailService {

    private static final String AUTHORIZE_URL = "/authorize";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String STATE_PARAM = "state";
    private static final String RESPONSE_TYPE_VALUE = "code";

    private final YandexProperties yandexProperties;

    private final YandexClient yandexClient;

    @Override
    public String getAuthUrl(Long userId) {
        String state = userId + " " + MailProvider.YANDEX;

        return UriComponentsBuilder.fromHttpUrl(yandexProperties.getAuthUrl() + AUTHORIZE_URL)
                .queryParam(RESPONSE_TYPE_PARAM, RESPONSE_TYPE_VALUE)
                .queryParam(CLIENT_ID_PARAM, yandexProperties.getClientId())
                .queryParam(STATE_PARAM, state)
                .build()
                .toUriString();
    }

    @Override
    public String getEmail(String token) {
        return yandexClient.getUserInfo(token).getDefaultEmail();
    }

    @Override
    public String getSettingsUrl() {
        return yandexProperties.getSettingsUrl();
    }

    @Override
    public AccessTokenResponseDTO getAccessTokenResponse(String code) {
        return yandexClient.getAccessToken(code);
    }

    @Override
    public RefreshTokenResponseDTO getRefreshTokenResponse(String refreshToken) {
        return yandexClient.refreshAccessToken(refreshToken);
    }

    @Override
    public MailProvider getMailProvider() {
        return MailProvider.YANDEX;
    }

    @Override
    public String getInstructionForPermission() {
        return """
                Теперь необходимо перейти по ссылке ниже и разрешить доступ к почтовому ящику с помощью почтовых клиентов.
                 Если Вы проходите авторизацию используя мобильный телефон:
                 1. После перехода по ссылке откройте боковое меню в верхнем левом углу.
                 2. Мотните вниз экрана и нажмите на кнопку "Полная версия".
                 3. Нажмите на шестеренку в крайнем правом углу и нажмите на кнопку "Все настройки".
                 4. В левой части экрана появится список, где необходимо выбрать "Почтовые программы".
                 5. Теперь Вы можете разрешить доступ к почтовому ящику с помощью почтовых клиентов
                """;
    }
}
