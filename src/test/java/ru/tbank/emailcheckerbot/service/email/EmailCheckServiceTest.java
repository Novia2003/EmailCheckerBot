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
import ru.tbank.emailcheckerbot.service.notification.NotificationService;
import ru.tbank.emailcheckerbot.service.user.UserEmailJpaService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailCheckServiceTest {

    @Mock
    private ExecutorService fixedThreadPool;

    @Mock
    private EmailSessionPropertiesService emailSessionPropertiesService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailSessionService emailSessionService;

    @Mock
    private UserEmailJpaService userEmailJpaService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private EmailCheckService emailCheckService;

    @Test
    void checkEmails_shouldSubmitTasksToThreadPool() {
        List<UserEmailJpaEntity> emails = Arrays.asList(new UserEmailJpaEntity(), new UserEmailJpaEntity());

        when(userEmailJpaService.getUserEmails()).thenReturn(emails);

        emailCheckService.checkEmails();

        verify(fixedThreadPool, times(2)).submit(any(Runnable.class));
    }

    @Test
    void checkForNewMessages_shouldRefreshTokenAndCheckForNewMessages() {
        UserEmailJpaEntity userEmailJpaEntity = new UserEmailJpaEntity();
        userEmailJpaEntity.setId(1L);
        userEmailJpaEntity.setEmail("slavik@mail.ru");
        userEmailJpaEntity.setAccessToken("accessToken");
        userEmailJpaEntity.setMailProvider(MailProvider.YANDEX);
        userEmailJpaEntity.setEndAccessTokenLife(Instant.now().minusSeconds(3600));
        userEmailJpaEntity.setLastMessageUID(100L);

        Properties properties = new Properties();
        Long newMessageUID = 101L;
        EmailMessageDTO[] newMessages = {
                new EmailMessageDTO(
                        newMessageUID,
                        "irishkakislova@mail.ru",
                        "<Без темы>",
                        "Все будет хорошо"
                )
        };

        when(emailSessionPropertiesService.getSessionProperties(MailProvider.YANDEX.getConfigurationName())).thenReturn(properties);
        when(emailSessionService.getNewMessages(properties, userEmailJpaEntity.getEmail(), userEmailJpaEntity.getAccessToken(), userEmailJpaEntity.getLastMessageUID())).thenReturn(newMessages);

        emailCheckService.checkForNewMessages(userEmailJpaEntity);

        verify(authenticationService).refreshToken(userEmailJpaEntity.getId(), true);
        verify(emailSessionService).getNewMessages(properties, userEmailJpaEntity.getEmail(), userEmailJpaEntity.getAccessToken(), userEmailJpaEntity.getLastMessageUID());
        verify(notificationService).notifyUser(newMessages, userEmailJpaEntity);
        verify(userEmailJpaService).setLastMessageUID(userEmailJpaEntity.getId(), newMessageUID);
    }

    @Test
    void checkForNewMessages_shouldNotRefreshTokenAndCheckForNewMessages() {
        UserEmailJpaEntity userEmailJpaEntity = new UserEmailJpaEntity();
        userEmailJpaEntity.setId(1L);
        userEmailJpaEntity.setEmail("test@example.com");
        userEmailJpaEntity.setAccessToken("accessToken");
        userEmailJpaEntity.setMailProvider(MailProvider.YANDEX);
        userEmailJpaEntity.setEndAccessTokenLife(Instant.now().plusSeconds(3600));
        userEmailJpaEntity.setLastMessageUID(100L);

        Properties properties = new Properties();
        Long newMessageUID = 101L;
        EmailMessageDTO[] newMessages = {
                new EmailMessageDTO(
                        newMessageUID,
                        "irishkakislova@mail.ru",
                        "<Без темы>",
                        "Все будет хорошо"
                )
        };

        when(emailSessionPropertiesService.getSessionProperties(MailProvider.YANDEX.getConfigurationName())).thenReturn(properties);
        when(emailSessionService.getNewMessages(properties, userEmailJpaEntity.getEmail(), userEmailJpaEntity.getAccessToken(), userEmailJpaEntity.getLastMessageUID())).thenReturn(newMessages);

        emailCheckService.checkForNewMessages(userEmailJpaEntity);

        verify(authenticationService, never()).refreshToken(anyLong(), anyBoolean());
        verify(emailSessionService).getNewMessages(properties, userEmailJpaEntity.getEmail(), userEmailJpaEntity.getAccessToken(), userEmailJpaEntity.getLastMessageUID());
        verify(notificationService).notifyUser(newMessages, userEmailJpaEntity);
        verify(userEmailJpaService).setLastMessageUID(userEmailJpaEntity.getId(), newMessageUID);
    }

    @Test
    void checkForNewMessages_shouldNotNotifyUserWhenNoNewMessages() {
        UserEmailJpaEntity userEmailJpaEntity = new UserEmailJpaEntity();
        userEmailJpaEntity.setId(1L);
        userEmailJpaEntity.setEmail("test@example.com");
        userEmailJpaEntity.setAccessToken("accessToken");
        userEmailJpaEntity.setMailProvider(MailProvider.YANDEX);
        userEmailJpaEntity.setEndAccessTokenLife(Instant.now().plusSeconds(3600));
        userEmailJpaEntity.setLastMessageUID(100L);

        Properties properties = new Properties();
        EmailMessageDTO[] newMessages = new EmailMessageDTO[0];

        when(emailSessionPropertiesService.getSessionProperties(MailProvider.YANDEX.getConfigurationName())).thenReturn(properties);
        when(emailSessionService.getNewMessages(properties, userEmailJpaEntity.getEmail(), userEmailJpaEntity.getAccessToken(), userEmailJpaEntity.getLastMessageUID())).thenReturn(newMessages);

        emailCheckService.checkForNewMessages(userEmailJpaEntity);

        verify(authenticationService, never()).refreshToken(anyLong(), anyBoolean());
        verify(emailSessionService).getNewMessages(properties, userEmailJpaEntity.getEmail(), userEmailJpaEntity.getAccessToken(), userEmailJpaEntity.getLastMessageUID());
        verify(notificationService, never()).notifyUser(any(), any());
        verify(userEmailJpaService, never()).setLastMessageUID(anyLong(), anyLong());
    }
}
