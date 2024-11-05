package ru.tbank.emailcheckerbot.integration.http.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.tbank.emailcheckerbot.dto.yandex.YandexUserInfoDTO;
import ru.tbank.emailcheckerbot.integration.http.YandexClient;

@Component
@RequiredArgsConstructor
public class YandexClientImpl implements YandexClient {

    private static final String INFO_URL = "/info?format=json";

    private final RestTemplate yandexRestTemplate;

    @Override
    public YandexUserInfoDTO getUserInfo(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "OAuth " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<YandexUserInfoDTO> response = yandexRestTemplate.exchange(
                INFO_URL,
                HttpMethod.GET,
                entity,
                YandexUserInfoDTO.class
        );

        return response.getBody();
    }
}