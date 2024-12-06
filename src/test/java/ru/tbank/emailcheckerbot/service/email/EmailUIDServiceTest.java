package ru.tbank.emailcheckerbot.service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.emailcheckerbot.dto.message.EmailMessageDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.exeption.DecryptionException;
import ru.tbank.emailcheckerbot.exeption.MessageNotFoundException;
import ru.tbank.emailcheckerbot.exeption.UserEmailJpaEntityNotFoundException;
import ru.tbank.emailcheckerbot.service.authentication.AuthenticationService;
import ru.tbank.emailcheckerbot.service.encryption.EncryptionService;
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

    @Mock
    private EncryptionService encryptionService;

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
        String encodedUserEmailId = "encodedUserEmailId";
        String encodedMessageUID = "encodedMessageUID";
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

        when(encryptionService.decodeId(encodedUserEmailId)).thenReturn(userEmailId);
        when(encryptionService.decodeId(encodedMessageUID)).thenReturn(messageUID);
        when(userEmailJpaService.getUserEmailJpaEntity(userEmailId)).thenReturn(userEmail);
        when(emailSessionPropertiesService.getSessionProperties(MailProvider.YANDEX.getConfigurationName()))
                .thenReturn(properties);
        when(emailSessionService.getMessageByUID(
                properties,
                userEmail.getEmail(),
                userEmail.getAccessToken(),
                messageUID
        )).thenReturn(emailMessageDTO);

        String result = emailUIDService.getMessageByUID(encodedUserEmailId, encodedMessageUID);

        assertEquals(expectedContent, result);
        verify(encryptionService).decodeId(encodedUserEmailId);
        verify(encryptionService).decodeId(encodedMessageUID);
        verify(userEmailJpaService).getUserEmailJpaEntity(userEmailId);
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
        String encodedUserEmailId = "encodedUserEmailId";
        String encodedMessageUID = "encodedMessageUID";
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

        when(encryptionService.decodeId(encodedUserEmailId)).thenReturn(userEmailId);
        when(encryptionService.decodeId(encodedMessageUID)).thenReturn(messageUID);
        when(userEmailJpaService.getUserEmailJpaEntity(userEmailId)).thenReturn(userEmail);
        when(emailSessionPropertiesService.getSessionProperties(MailProvider.YANDEX.getConfigurationName()))
                .thenReturn(properties);
        when(emailSessionService.getMessageByUID(
                properties,
                userEmail.getEmail(),
                userEmail.getAccessToken(),
                messageUID
        )).thenReturn(emailMessageDTO);

        String result = emailUIDService.getMessageByUID(encodedUserEmailId, encodedMessageUID);

        assertEquals(expectedContent, result);
        verify(encryptionService).decodeId(encodedUserEmailId);
        verify(encryptionService).decodeId(encodedMessageUID);
        verify(userEmailJpaService).getUserEmailJpaEntity(userEmailId);
        verify(emailSessionPropertiesService).getSessionProperties(MailProvider.YANDEX.getConfigurationName());
        verify(emailSessionService).getMessageByUID(
                properties,
                userEmail.getEmail(),
                userEmail.getAccessToken(),
                messageUID
        );
        verify(authenticationService).refreshToken(userEmailId, true);
    }

    @Test
    void getMessageByUID_shouldReturnErrorMessageWhenDecryptionFails() {
        String encodedUserEmailId = "encodedUserEmailId";
        String encodedMessageUID = "encodedMessageUID";

        when(encryptionService.decodeId(encodedUserEmailId)).thenThrow(
                new DecryptionException("Decryption failed", new RuntimeException())
        );

        String result = emailUIDService.getMessageByUID(encodedUserEmailId, encodedMessageUID);

        assertEquals("Произошла ошибка при расшифровании входных параметров", result);
        verify(encryptionService).decodeId(encodedUserEmailId);
        verify(encryptionService, never()).decodeId(encodedMessageUID);
        verify(userEmailJpaService, never()).getUserEmailJpaEntity(anyLong());
        verify(emailSessionPropertiesService, never()).getSessionProperties(anyString());
        verify(emailSessionService, never()).getMessageByUID(any(), any(), any(), anyLong());
        verify(authenticationService, never()).refreshToken(anyLong(), anyBoolean());
    }

    @Test
    void getMessageByUID_shouldReturnErrorMessageWhenUserEmailNotFound() {
        String encodedUserEmailId = "encodedUserEmailId";
        String encodedMessageUID = "encodedMessageUID";
        long userEmailId = 1L;
        long messageUID = 100L;

        when(encryptionService.decodeId(encodedUserEmailId)).thenReturn(userEmailId);
        when(encryptionService.decodeId(encodedMessageUID)).thenReturn(messageUID);
        when(userEmailJpaService.getUserEmailJpaEntity(userEmailId)).thenThrow(new UserEmailJpaEntityNotFoundException("User email not found"));

        String result = emailUIDService.getMessageByUID(encodedUserEmailId, encodedMessageUID);

        assertEquals("Запись о почте с encodedUserEmailId " + encodedUserEmailId + " не найдена", result);
        verify(encryptionService).decodeId(encodedUserEmailId);
        verify(encryptionService).decodeId(encodedMessageUID);
        verify(userEmailJpaService).getUserEmailJpaEntity(userEmailId);
        verify(emailSessionPropertiesService, never()).getSessionProperties(anyString());
        verify(emailSessionService, never()).getMessageByUID(any(), any(), any(), anyLong());
        verify(authenticationService, never()).refreshToken(anyLong(), anyBoolean());
    }

    @Test
    void getMessageByUID_shouldReturnErrorMessageWhenMessageNotFound() {
        String encodedUserEmailId = "encodedUserEmailId";
        String encodedMessageUID = "encodedMessageUID";
        long userEmailId = 1L;
        long messageUID = 100L;
        UserEmailJpaEntity userEmail = new UserEmailJpaEntity();
        userEmail.setId(userEmailId);
        userEmail.setEmail("slavik@mail.ru");
        userEmail.setAccessToken("accessToken");
        userEmail.setMailProvider(MailProvider.YANDEX);
        userEmail.setEndAccessTokenLife(Instant.now().plusSeconds(3600));
        Properties properties = new Properties();

        when(encryptionService.decodeId(encodedUserEmailId)).thenReturn(userEmailId);
        when(encryptionService.decodeId(encodedMessageUID)).thenReturn(messageUID);
        when(userEmailJpaService.getUserEmailJpaEntity(userEmailId)).thenReturn(userEmail);
        when(emailSessionPropertiesService.getSessionProperties(MailProvider.YANDEX.getConfigurationName()))
                .thenReturn(properties);
        when(emailSessionService.getMessageByUID(
                properties,
                userEmail.getEmail(),
                userEmail.getAccessToken(),
                messageUID
        )).thenThrow(new MessageNotFoundException("Message not found"));

        String result = emailUIDService.getMessageByUID(encodedUserEmailId, encodedMessageUID);

        assertEquals("Сообщение с encodeMessageUID " + encodedMessageUID + "не найдено", result);
        verify(encryptionService).decodeId(encodedUserEmailId);
        verify(encryptionService).decodeId(encodedMessageUID);
        verify(userEmailJpaService).getUserEmailJpaEntity(userEmailId);
        verify(emailSessionPropertiesService).getSessionProperties(MailProvider.YANDEX.getConfigurationName());
        verify(emailSessionService).getMessageByUID(
                properties,
                userEmail.getEmail(),
                userEmail.getAccessToken(),
                messageUID
        );
        verify(authenticationService, never()).refreshToken(anyLong(), anyBoolean());
    }
}
