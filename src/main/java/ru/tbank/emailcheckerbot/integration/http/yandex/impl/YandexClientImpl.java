package ru.tbank.emailcheckerbot.integration.http.yandex.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.tbank.emailcheckerbot.configuration.property.provider.YandexProperties;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.yandex.YandexUserInfoDTO;
import ru.tbank.emailcheckerbot.integration.http.yandex.YandexClient;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class YandexClientImpl implements YandexClient {

    private static final String INFO_URL = "/info?format=json";
    private static final String TOKEN_URL = "/token";

    private final RestTemplate yandexUserInfoRestTemplate;
    private final RestTemplate yandexOauthRestTemplate;

    private final YandexProperties yandexProperties;

    @Override
    public YandexUserInfoDTO getUserInfo(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "OAuth " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<YandexUserInfoDTO> response = yandexUserInfoRestTemplate.exchange(
                INFO_URL,
                HttpMethod.GET,
                entity,
                YandexUserInfoDTO.class
        );

        return response.getBody();
    }

    @Override
    public AccessTokenResponseDTO getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String token = Base64.getEncoder().encodeToString((
                yandexProperties.getClientId() + ":" + yandexProperties.getClientSecret()
        ).getBytes());
        headers.set("Authorization", "Basic " + token);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<AccessTokenResponseDTO> response = yandexOauthRestTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                requestEntity,
                AccessTokenResponseDTO.class
        );

        return response.getBody();
    }

    @Override
    public RefreshTokenResponseDTO refreshAccessToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String token = Base64.getEncoder().encodeToString((
                yandexProperties.getClientId() + ":" + yandexProperties.getClientSecret()
        ).getBytes());
        headers.set("Authorization", "Basic " + token);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<RefreshTokenResponseDTO> response = yandexOauthRestTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                requestEntity,
                RefreshTokenResponseDTO.class
        );

        return response.getBody();
    }
}