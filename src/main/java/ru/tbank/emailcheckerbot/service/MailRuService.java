package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ru.tbank.emailcheckerbot.entity.MailProvider;
import ru.tbank.emailcheckerbot.configuration.property.MailRuProperties;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.integration.http.mailru.MailRuClient;

@Service
@RequiredArgsConstructor
public class MailRuService {

    private static final String LOGIN_URL = "/login";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String SCOPE_PARAM = "scope";
    private static final String STATE_PARAM = "state";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String RESPONSE_TYPE_VALUE = "code";
    private static final String SCOPE_VALUE = "userinfo mail.imap";

    private final MailRuProperties mailRuProperties;

    private final MailRuClient mailRuClient;

    public String getAuthUrl(Long userId) {
        String state = userId + " " + MailProvider.MAILRu;

        return UriComponentsBuilder.fromHttpUrl(mailRuProperties.getUrl() + LOGIN_URL)
                .queryParam(CLIENT_ID_PARAM, mailRuProperties.getClientId())
                .queryParam(RESPONSE_TYPE_PARAM, RESPONSE_TYPE_VALUE)
                .queryParam(SCOPE_PARAM, SCOPE_VALUE)
                .queryParam(STATE_PARAM, state)
                .queryParam(REDIRECT_URI_PARAM, mailRuProperties.getRedirectUri())
                .build()
                .toUriString();
    }

    public AccessTokenResponseDTO getAccessToken(String code) {
        return mailRuClient.getAccessToken(code);
    }

    public String getSettingsUrl() {
        return mailRuProperties.getSettingsUrl();
    }

    public String getEmail(String token) {return mailRuClient.getUserInfo(token).getEmail();}

    public RefreshTokenResponseDTO getRefreshTokenResponse(String refreshToken) {
        return mailRuClient.refreshAccessToken(refreshToken);
    }
}
