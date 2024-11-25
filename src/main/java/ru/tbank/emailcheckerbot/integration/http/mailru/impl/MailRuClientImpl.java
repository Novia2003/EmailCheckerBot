package ru.tbank.emailcheckerbot.integration.http.mailru.impl;

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
import ru.tbank.emailcheckerbot.configuration.property.RedirectUriProperties;
import ru.tbank.emailcheckerbot.configuration.property.provider.MailRuProperties;
import ru.tbank.emailcheckerbot.dto.mailru.MailRuUserInfoDTO;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.integration.http.mailru.MailRuClient;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class MailRuClientImpl implements MailRuClient {

    private static final String TOKEN_URL = "/token";
    private static final String INFO_URL = "/userinfo";

    private final MailRuProperties mailRuProperties;
    private final RedirectUriProperties redirectUriProperties;

    private final RestTemplate mailRuRestTemplate;

    @Override
    public AccessTokenResponseDTO getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String token = Base64.getEncoder().encodeToString((
                mailRuProperties.getClientId() + ":" + mailRuProperties.getClientSecret()
        ).getBytes());
        headers.set("Authorization", "Basic " + token);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUriProperties.getUri());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<AccessTokenResponseDTO> response = mailRuRestTemplate.exchange(
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

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", mailRuProperties.getClientId());
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<RefreshTokenResponseDTO> response = mailRuRestTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                requestEntity,
                RefreshTokenResponseDTO.class
        );

        return response.getBody();
    }

    @Override
    public MailRuUserInfoDTO getUserInfo(String token) {

        ResponseEntity<MailRuUserInfoDTO> response = mailRuRestTemplate.getForEntity(
                INFO_URL + "?access_token=" + token,
                MailRuUserInfoDTO.class
        );

        return response.getBody();
    }
}
