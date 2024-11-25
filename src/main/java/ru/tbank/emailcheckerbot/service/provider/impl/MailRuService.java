package ru.tbank.emailcheckerbot.service.provider.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ru.tbank.emailcheckerbot.configuration.property.RedirectUriProperties;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.configuration.property.provider.MailRuProperties;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.integration.http.mailru.MailRuClient;
import ru.tbank.emailcheckerbot.service.provider.MailService;

@Service
@RequiredArgsConstructor
public class MailRuService implements MailService {

    private static final String LOGIN_URL = "/login";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String SCOPE_PARAM = "scope";
    private static final String STATE_PARAM = "state";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String RESPONSE_TYPE_VALUE = "code";
    private static final String SCOPE_VALUE = "userinfo mail.imap";

    private final MailRuProperties mailRuProperties;
    private final RedirectUriProperties redirectUriProperties;

    private final MailRuClient mailRuClient;

    @Override
    public String getAuthUrl(Long userId) {
        String state = userId + " " + MailProvider.MAILRu;

        return UriComponentsBuilder.fromHttpUrl(mailRuProperties.getAuthUrl() + LOGIN_URL)
                .queryParam(CLIENT_ID_PARAM, mailRuProperties.getClientId())
                .queryParam(RESPONSE_TYPE_PARAM, RESPONSE_TYPE_VALUE)
                .queryParam(SCOPE_PARAM, SCOPE_VALUE)
                .queryParam(STATE_PARAM, state)
                .queryParam(REDIRECT_URI_PARAM, redirectUriProperties.getUri())
                .build()
                .toUriString();
    }

    @Override
    public AccessTokenResponseDTO getAccessTokenResponse(String code) {
        return mailRuClient.getAccessToken(code);
    }

    @Override
    public String getSettingsUrl() {
        return mailRuProperties.getSettingsUrl();
    }

    @Override
    public String getEmail(String token) {return mailRuClient.getUserInfo(token).getEmail();}

    @Override
    public RefreshTokenResponseDTO getRefreshTokenResponse(String refreshToken) {
        return mailRuClient.refreshAccessToken(refreshToken);
    }

    @Override
    public MailProvider getMailProvider() {
        return MailProvider.MAILRu;
    }

    @Override
    public String getInstructionForPermission() {
        return "Теперь необходимо перейти по ссылке ниже и " +
                "разрешить доступ к почтовому ящику с помощью почтовых клиентов, следуя описанной там инструкции";
    }
}
