package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.message.EmailMessage;

import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailCheckService {

    private final ExecutorService fixedThreadPool;
    private final EmailSessionPropertiesService emailSessionPropertiesService;
    private final NotificationService notificationService;
    private final EmailSessionService emailSessionService;
    private final UserEmailJpaService userEmailJpaService;
    private final AuthenticationService authenticationService;

    @Scheduled(fixedDelayString = "${email.check.delay}")
    public void checkEmails() {
        log.info("Starting email check process");

        List<UserEmailJpaEntity> emails = userEmailJpaService.getUserEmails();
        for (UserEmailJpaEntity email : emails) {
            fixedThreadPool.submit(() -> checkForNewMessages(email));
        }

        log.info("Email check process completed");
    }

    private void checkForNewMessages(UserEmailJpaEntity userEmailJpaEntity) {
        Properties properties = emailSessionPropertiesService.getSessionProperties(
                userEmailJpaEntity.getMailProvider().getConfigurationName()
        );

        if (Instant.now().isAfter(userEmailJpaEntity.getEndAccessTokenLife())) {
            log.info("Access token expired for user: {}. Refreshing token...", userEmailJpaEntity.getEmail());
            authenticationService.refreshToken(userEmailJpaEntity.getId(), true);
        }

        EmailMessage[] newMessages = emailSessionService.getNewMessages(
                properties,
                userEmailJpaEntity.getEmail(),
                userEmailJpaEntity.getAccessToken(),
                userEmailJpaEntity.getLastMessageUID()
        );

        if (newMessages.length > 0) {
            log.info("New messages found for user: {}", userEmailJpaEntity.getEmail());
            notificationService.notifyUser(newMessages, userEmailJpaEntity);

            Long lastMessageUID = newMessages[newMessages.length - 1].getUid();
            userEmailJpaService.setLastMessageUID(userEmailJpaEntity.getId(), lastMessageUID);
        } else {
            log.info("No new messages found for user: {}", userEmailJpaEntity.getEmail());
        }
    }
}
