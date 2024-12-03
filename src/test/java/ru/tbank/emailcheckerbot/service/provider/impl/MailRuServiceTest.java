package ru.tbank.emailcheckerbot.service.provider.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.configuration.property.RedirectUriProperties;
import ru.tbank.emailcheckerbot.configuration.property.provider.MailRuProperties;
import ru.tbank.emailcheckerbot.dto.mailru.MailRuUserInfoDTO;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.integration.http.mailru.MailRuClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailRuServiceTest {

    @Mock
    private MailRuProperties mailRuProperties;

    @Mock
    private RedirectUriProperties redirectUriProperties;

    @Mock
    private MailRuClient mailRuClient;

    @InjectMocks
    private MailRuService mailRuService;

    @Test
    void getAuthUrl_shouldReturnCorrectAuthUrl() {
        Long userId = 1L;
        String authUrl = "https://ouath.mail.ru/login";
        String clientId = "clientId";
        String redirectUri = "https://ouath.mail.ru/redirect";

        when(mailRuProperties.getAuthUrl()).thenReturn(authUrl);
        when(mailRuProperties.getClientId()).thenReturn(clientId);
        when(redirectUriProperties.getUri()).thenReturn(redirectUri);

        String expectedUrl = "https://ouath.mail.ru/login/login?client_id=clientId&response_type=code&scope=userinfo mail.imap&state=1 MAILRu&redirect_uri=https://ouath.mail.ru/redirect";
        String result = mailRuService.getAuthUrl(userId);

        assertEquals(expectedUrl, result);
    }

    @Test
    void getAccessTokenResponse_shouldReturnAccessTokenResponse() {
        String code = "authCode";
        AccessTokenResponseDTO expectedResponse = new AccessTokenResponseDTO();
        expectedResponse.setAccessToken("accessToken");

        when(mailRuClient.getAccessToken(code)).thenReturn(expectedResponse);

        AccessTokenResponseDTO result = mailRuService.getAccessTokenResponse(code);

        assertEquals(expectedResponse, result);
        verify(mailRuClient).getAccessToken(code);
    }

    @Test
    void getSettingsUrl_shouldReturnSettingsUrl() {
        String settingsUrl = "https://mail.ru/settings";

        when(mailRuProperties.getSettingsUrl()).thenReturn(settingsUrl);

        String result = mailRuService.getSettingsUrl();

        assertEquals(settingsUrl, result);
    }

    @Test
    void getEmail_shouldReturnEmail() {
        String token = "accessToken";
        String expectedEmail = "slavik@mail.ru";
        MailRuUserInfoDTO userInfoResponse = new MailRuUserInfoDTO();
        userInfoResponse.setEmail(expectedEmail);

        when(mailRuClient.getUserInfo(token)).thenReturn(userInfoResponse);

        String result = mailRuService.getEmail(token);

        assertEquals(expectedEmail, result);
        verify(mailRuClient).getUserInfo(token);
    }

    @Test
    void getRefreshTokenResponse_shouldReturnRefreshTokenResponse() {
        String refreshToken = "refreshToken";
        RefreshTokenResponseDTO expectedResponse = new RefreshTokenResponseDTO();
        expectedResponse.setAccessToken("newAccessToken");

        when(mailRuClient.refreshAccessToken(refreshToken)).thenReturn(expectedResponse);

        RefreshTokenResponseDTO result = mailRuService.getRefreshTokenResponse(refreshToken);

        assertEquals(expectedResponse, result);
        verify(mailRuClient).refreshAccessToken(refreshToken);
    }

    @Test
    void getMailProvider_shouldReturnMailRuProvider() {
        MailProvider result = mailRuService.getMailProvider();

        assertEquals(MailProvider.MAILRu, result);
    }

    @Test
    void getInstructionForPermission_shouldReturnInstruction() {
        String expectedInstruction = "Теперь необходимо перейти по ссылке ниже и " +
                "разрешить доступ к почтовому ящику с помощью почтовых клиентов, следуя описанной там инструкции";

        String result = mailRuService.getInstructionForPermission();

        assertEquals(expectedInstruction, result);
    }
}
