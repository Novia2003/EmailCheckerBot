package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.configuration.property.YandexProperties;
import ru.tbank.emailcheckerbot.integration.http.YandexClient;

@Service
@RequiredArgsConstructor
public class YandexService {

    private final YandexProperties yandexProperties;

    private final YandexClient yandexClient;

    public String getAuthUrl() {
        return yandexProperties.getAuthUrl() + yandexProperties.getClientId();
    }

    public String getEmail(String token) {
        return yandexClient.getUserInfo(token).getDefaultEmail();
    }

    public String getSettingsUrl() {
        return yandexProperties.getSettingsUrl();
    }
}
