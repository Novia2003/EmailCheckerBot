package ru.tbank.emailcheckerbot.service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.dto.message.EmailMessageDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.service.authentication.AuthenticationService;
import ru.tbank.emailcheckerbot.service.user.UserEmailJpaService;

import java.time.Instant;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailUIDServiceTest {

    @Mock
    private EmailSessionPropertiesService emailSessionPropertiesService;

    @Mock
    private EmailSessionService emailSessionService;

    @Mock
    private UserEmailJpaService userEmailJpaService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private EmailUIDService emailUIDService;

    @Test
    void getLastMessageUID_shouldReturnLastMessageUID() {
        String email = "slavik@mail.ru";
        MailProvider emailProvider = MailProvider.YANDEX;
        String token = "token";
        Properties properties = new Properties();
        long expectedLastMessageUID = 100L;

        when(emailSessionPropertiesService.getSessionProperties(emailProvider.getConfigurationName()))
                .thenReturn(properties);
        when(emailSessionService.getLastMessageUID(properties, email, token)).thenReturn(expectedLastMessageUID);

        long result = emailUIDService.getLastMessageUID(email, emailProvider, token);

        assertEquals(expectedLastMessageUID, result);
        verify(emailSessionPropertiesService).getSessionProperties(emailProvider.getConfigurationName());
        verify(emailSessionService).getLastMessageUID(properties, email, token);
    }

    @Test
    void getMessageByUID_shouldReturnMessageContentWhenTokenIsValid() {
        long userEmailId = 1L;
        long messageUID = 100L;
        String expectedContent = "Все будет хорошо";
        UserEmailJpaEntity userEmail = new UserEmailJpaEntity();
        userEmail.setId(userEmailId);
        userEmail.setEmail("slavik@mail.ru");
        userEmail.setAccessToken("accessToken");
        userEmail.setMailProvider(MailProvider.YANDEX);
        userEmail.setEndAccessTokenLife(Instant.now().plusSeconds(3600));
        Properties properties = new Properties();
        EmailMessageDTO emailMessageDTO = new EmailMessageDTO(
                100L,
                "irishkakislova@mail.ru",
                "<Без темы>",
                expectedContent
        );

        when(userEmailJpaService.getUserEmail(userEmailId)).thenReturn(userEmail);
        when(emailSessionPropertiesService.getSessionProperties(MailProvider.YANDEX.getConfigurationName()))
                .thenReturn(properties);
        when(emailSessionService.getMessageByUID(
                properties,
                userEmail.getEmail(),
                userEmail.getAccessToken(),
                messageUID
        )).thenReturn(emailMessageDTO);

        String result = emailUIDService.getMessageByUID(userEmailId, messageUID);

        assertEquals(expectedContent, result);
        verify(userEmailJpaService).getUserEmail(userEmailId);
        verify(emailSessionPropertiesService).getSessionProperties(MailProvider.YANDEX.getConfigurationName());
        verify(emailSessionService).getMessageByUID(
                properties,
                userEmail.getEmail(),
                userEmail.getAccessToken(),
                messageUID);
        verify(authenticationService, never()).refreshToken(anyLong(), anyBoolean());
    }

    @Test
    void getMessageByUID_shouldRefreshTokenAndReturnMessageContentWhenTokenIsExpired() {
        long userEmailId = 1L;
        long messageUID = 100L;
        String expectedContent = "Все будет хорошо";
        UserEmailJpaEntity userEmail = new UserEmailJpaEntity();
        userEmail.setId(userEmailId);
        userEmail.setEmail("slavik@mail.ru");
        userEmail.setAccessToken("accessToken");
        userEmail.setMailProvider(MailProvider.YANDEX);
        userEmail.setEndAccessTokenLife(Instant.now().minusSeconds(3600));
        Properties properties = new Properties();
        EmailMessageDTO emailMessageDTO = new EmailMessageDTO(
                100L,
                "irishkakislova@mail.ru",
                "<Без темы>",
                expectedContent
        );

        when(userEmailJpaService.getUserEmail(userEmailId)).thenReturn(userEmail);
        when(emailSessionPropertiesService.getSessionProperties(MailProvider.YANDEX.getConfigurationName()))
                .thenReturn(properties);
        when(emailSessionService.getMessageByUID(
                properties,
                userEmail.getEmail(),
                userEmail.getAccessToken(),
                messageUID
        )).thenReturn(emailMessageDTO);

        String result = emailUIDService.getMessageByUID(userEmailId, messageUID);

        assertEquals(expectedContent, result);
        verify(userEmailJpaService).getUserEmail(userEmailId);
        verify(emailSessionPropertiesService).getSessionProperties(MailProvider.YANDEX.getConfigurationName());
        verify(emailSessionService).getMessageByUID(
                properties,
                userEmail.getEmail(),
                userEmail.getAccessToken(),
                messageUID
        );
        verify(authenticationService).refreshToken(userEmailId, true);
    }
}
