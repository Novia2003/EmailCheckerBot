package ru.tbank.emailcheckerbot.service.provider.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.configuration.property.provider.YandexProperties;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.dto.yandex.YandexUserInfoDTO;
import ru.tbank.emailcheckerbot.integration.http.yandex.YandexClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YandexServiceTest {

    @Mock
    private YandexProperties yandexProperties;

    @Mock
    private YandexClient yandexClient;

    @InjectMocks
    private YandexService yandexService;

    @Test
    void getAuthUrl_shouldReturnCorrectAuthUrl() {
        Long userId = 1L;
        String authUrl = "https://ouath.yandex.com/authorize";
        String clientId = "clientId";

        when(yandexProperties.getAuthUrl()).thenReturn(authUrl);
        when(yandexProperties.getClientId()).thenReturn(clientId);

        String expectedUrl = "https://ouath.yandex.com/authorize/authorize?response_type=code&client_id=clientId&state=1 YANDEX";
        String result = yandexService.getAuthUrl(userId);

        assertEquals(expectedUrl, result);
    }

    @Test
    void getEmail_shouldReturnEmail() {
        String token = "accessToken";
        String expectedEmail = "slavik@ya.com";
        YandexUserInfoDTO userInfoResponse = new YandexUserInfoDTO();
        userInfoResponse.setDefaultEmail(expectedEmail);

        when(yandexClient.getUserInfo(token)).thenReturn(userInfoResponse);

        String result = yandexService.getEmail(token);

        assertEquals(expectedEmail, result);
        verify(yandexClient).getUserInfo(token);
    }

    @Test
    void getSettingsUrl_shouldReturnSettingsUrl() {
        String settingsUrl = "https://yandex.mail.com/settings";

        when(yandexProperties.getSettingsUrl()).thenReturn(settingsUrl);

        String result = yandexService.getSettingsUrl();

        assertEquals(settingsUrl, result);
    }

    @Test
    void getAccessTokenResponse_shouldReturnAccessTokenResponse() {
        String code = "authCode";
        AccessTokenResponseDTO expectedResponse = new AccessTokenResponseDTO();
        expectedResponse.setAccessToken("accessToken");

        when(yandexClient.getAccessToken(code)).thenReturn(expectedResponse);

        AccessTokenResponseDTO result = yandexService.getAccessTokenResponse(code);

        assertEquals(expectedResponse, result);
        verify(yandexClient).getAccessToken(code);
    }

    @Test
    void getRefreshTokenResponse_shouldReturnRefreshTokenResponse() {
        String refreshToken = "refreshToken";
        RefreshTokenResponseDTO expectedResponse = new RefreshTokenResponseDTO();
        expectedResponse.setAccessToken("newAccessToken");

        when(yandexClient.refreshAccessToken(refreshToken)).thenReturn(expectedResponse);

        RefreshTokenResponseDTO result = yandexService.getRefreshTokenResponse(refreshToken);

        assertEquals(expectedResponse, result);
        verify(yandexClient).refreshAccessToken(refreshToken);
    }

    @Test
    void getMailProvider_shouldReturnYandexProvider() {
        MailProvider result = yandexService.getMailProvider();

        assertEquals(MailProvider.YANDEX, result);
    }

    @Test
    void getInstructionForPermission_shouldReturnInstruction() {
        String expectedInstruction = """
                Теперь необходимо перейти по ссылке ниже и разрешить доступ к почтовому ящику с помощью почтовых клиентов.
                 Если Вы проходите авторизацию используя мобильный телефон:
                 1. После перехода по ссылке откройте боковое меню в верхнем левом углу.
                 2. Мотните вниз экрана и нажмите на кнопку "Полная версия".
                 3. Нажмите на шестеренку в крайнем правом углу и нажмите на кнопку "Все настройки".
                 4. В левой части экрана появится список, где необходимо выбрать "Почтовые программы".
                 5. Теперь Вы можете разрешить доступ к почтовому ящику с помощью почтовых клиентов
                """;

        String result = yandexService.getInstructionForPermission();

        assertEquals(expectedInstruction, result);
    }
}
