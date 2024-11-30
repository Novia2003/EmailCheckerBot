package ru.tbank.emailcheckerbot.service.authentication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.exeption.UserEmailRedisEntityNotFoundException;
import ru.tbank.emailcheckerbot.service.provider.MailService;
import ru.tbank.emailcheckerbot.service.provider.factory.MailServiceFactory;
import ru.tbank.emailcheckerbot.service.user.UserEmailJpaService;
import ru.tbank.emailcheckerbot.service.user.UserEmailRedisService;
import ru.tbank.emailcheckerbot.service.user.UserEmailService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserEmailService userEmailService;

    @Mock
    private UserEmailRedisService userEmailRedisService;

    @Mock
    private UserEmailJpaService userEmailJpaService;

    @Mock
    private MailServiceFactory mailServiceFactory;

    @Mock
    private MailService mailService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void authenticate_shouldReturnUserInactivityMessageWhenUserEmailRecordNotExists() {
        String state = "1 YANDEX";
        String code = "authCode";
        Long userId = 1L;

        when(userEmailRedisService.isUserEmailRecordNotExists(userId)).thenReturn(true);

        String result = authenticationService.authenticate(state, code);

        assertEquals("Вы долго бездействовали, и запись о Вас была удалена.\n" +
                "Пожалуйста, начните процесс добавления почты с начала, набрав команду /add_email", result);
    }

    @Test
    void authenticate_shouldReturnEmailAlreadyRegisteredMessageWhenEmailAlreadyRegistered() {
        String state = "1 YANDEX";
        String code = "authCode";
        Long userId = 1L;
        MailProvider provider = MailProvider.YANDEX;
        String email = "slavik@mail.ru";
        AccessTokenResponseDTO accessTokenResponse = new AccessTokenResponseDTO();
        accessTokenResponse.setAccessToken("accessToken");

        when(userEmailRedisService.isUserEmailRecordNotExists(userId)).thenReturn(false);
        when(mailServiceFactory.getService(provider)).thenReturn(mailService);
        when(mailService.getAccessTokenResponse(code)).thenReturn(accessTokenResponse);
        when(mailService.getEmail(accessTokenResponse.getAccessToken())).thenReturn(email);
        when(userEmailJpaService.existsByUserIdAndEmail(userId, email)).thenReturn(true);

        String result = authenticationService.authenticate(state, code);

        assertEquals("Почта slavik@mail.ru уже была зарегистрирована. Регистрация прекращена", result);
        verify(userEmailRedisService).deleteEntity(userId);
    }

    @Test
    void authenticate_shouldReturnSuccessfulGettingTokenMessageWhenEmailNotRegistered() {
        String state = "1 YANDEX";
        String code = "authCode";
        Long userId = 1L;
        MailProvider provider = MailProvider.YANDEX;
        String email = "slavik@mail.ru";
        AccessTokenResponseDTO accessTokenResponse = new AccessTokenResponseDTO();
        accessTokenResponse.setAccessToken("accessToken");

        when(userEmailRedisService.isUserEmailRecordNotExists(userId)).thenReturn(false);
        when(mailServiceFactory.getService(provider)).thenReturn(mailService);
        when(mailService.getAccessTokenResponse(code)).thenReturn(accessTokenResponse);
        when(mailService.getEmail(accessTokenResponse.getAccessToken())).thenReturn(email);
        when(userEmailJpaService.existsByUserIdAndEmail(userId, email)).thenReturn(false);

        String result = authenticationService.authenticate(state, code);

        assertEquals("Токен для работы с почтой slavik@mail.ru успешно получен.\n" +
                "Можете вернуться в чат и смело нажать кнопку \"Выполнено\"", result);
        verify(userEmailRedisService).saveAccessTokenResponseAndEmail(userId, accessTokenResponse, provider, email);
    }

    @Test
    void authenticate_shouldReturnUserInactivityMessageWhenUserEmailRedisEntityNotFoundException() {
        String state = "1 YANDEX";
        String code = "authCode";
        Long userId = 1L;
        MailProvider provider = MailProvider.YANDEX;
        String email = "slavik@mail.ru";
        AccessTokenResponseDTO accessTokenResponse = new AccessTokenResponseDTO();
        accessTokenResponse.setAccessToken("accessToken");

        when(userEmailRedisService.isUserEmailRecordNotExists(userId)).thenReturn(false);
        when(mailServiceFactory.getService(provider)).thenReturn(mailService);
        when(mailService.getAccessTokenResponse(code)).thenReturn(accessTokenResponse);
        when(mailService.getEmail(accessTokenResponse.getAccessToken())).thenReturn(email);
        when(userEmailJpaService.existsByUserIdAndEmail(userId, email)).thenReturn(false);
        doThrow(new UserEmailRedisEntityNotFoundException("Entity not found")).when(userEmailRedisService).saveAccessTokenResponseAndEmail(anyLong(), any(), any(), anyString());

        String result = authenticationService.authenticate(state, code);

        assertEquals("Вы долго бездействовали, и запись о Вас была удалена.\n" +
                "Пожалуйста, начните процесс добавления почты с начала, набрав команду /add_email", result);
    }

    @Test
    void refreshToken_shouldRefreshTokenForRegisteredEmail() {
        Long id = 1L;
        boolean isEmailRegistered = true;
        MailProvider provider = MailProvider.YANDEX;
        String refreshToken = "refreshToken";
        RefreshTokenResponseDTO refreshTokenResponseDTO = new RefreshTokenResponseDTO();
        refreshTokenResponseDTO.setAccessToken("newAccessToken");

        when(userEmailService.getMailProvider(id, isEmailRegistered)).thenReturn(provider);
        when(userEmailService.getRefreshToken(id, isEmailRegistered)).thenReturn(refreshToken);
        when(mailServiceFactory.getService(provider)).thenReturn(mailService);
        when(mailService.getRefreshTokenResponse(refreshToken)).thenReturn(refreshTokenResponseDTO);

        authenticationService.refreshToken(id, isEmailRegistered);

        verify(userEmailService).saveRefreshTokenResponse(id, isEmailRegistered, refreshTokenResponseDTO);
    }

    @Test
    void refreshToken_shouldRefreshTokenForUnregisteredEmail() {
        Long id = 1L;
        boolean isEmailRegistered = false;
        MailProvider provider = MailProvider.YANDEX;
        String refreshToken = "refreshToken";
        RefreshTokenResponseDTO refreshTokenResponseDTO = new RefreshTokenResponseDTO();
        refreshTokenResponseDTO.setAccessToken("newAccessToken");

        when(userEmailService.getMailProvider(id, isEmailRegistered)).thenReturn(provider);
        when(userEmailService.getRefreshToken(id, isEmailRegistered)).thenReturn(refreshToken);
        when(mailServiceFactory.getService(provider)).thenReturn(mailService);
        when(mailService.getRefreshTokenResponse(refreshToken)).thenReturn(refreshTokenResponseDTO);

        authenticationService.refreshToken(id, isEmailRegistered);

        verify(userEmailService).saveRefreshTokenResponse(id, isEmailRegistered, refreshTokenResponseDTO);
    }
}
