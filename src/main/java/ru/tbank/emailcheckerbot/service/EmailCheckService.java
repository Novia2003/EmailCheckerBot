package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.entity.postgre.UserEmailPostgreEntity;
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
    private final UserEmailPostgreService userEmailPostgreService;
    private final AuthenticationService authenticationService;

    @Scheduled(fixedDelayString = "${email.check.delay}")
    public void checkEmails() {
        log.info("Starting email check process");

        List<UserEmailPostgreEntity> emails = userEmailPostgreService.getUserEmails();
        for (UserEmailPostgreEntity email : emails) {
            fixedThreadPool.submit(() -> checkForNewMessages(email));
        }

        log.info("Email check process completed");
    }

    private void checkForNewMessages(UserEmailPostgreEntity userEmailPostgreEntity) {
        Properties properties = emailSessionPropertiesService.getSessionProperties(
                userEmailPostgreEntity.getMailProvider().getConfigurationName()
        );

        if (Instant.now().isAfter(userEmailPostgreEntity.getEndAccessTokenLife())) {
            log.info("Access token expired for user: {}. Refreshing token...", userEmailPostgreEntity.getEmail());
            authenticationService.refreshToken(userEmailPostgreEntity.getId(), true);
        }

        EmailMessage[] newMessages = emailSessionService.getNewMessages(
                properties,
                userEmailPostgreEntity.getEmail(),
                userEmailPostgreEntity.getAccessToken(),
                userEmailPostgreEntity.getLastMessageUID()
        );

        if (newMessages.length > 0) {
            log.info("New messages found for user: {}", userEmailPostgreEntity.getEmail());
            notificationService.notifyUser(newMessages, userEmailPostgreEntity);

            Long lastMessageUID = newMessages[newMessages.length - 1].getUid();
            userEmailPostgreService.setLastMessageUID(userEmailPostgreEntity.getId(), lastMessageUID);
        } else {
            log.info("No new messages found for user: {}", userEmailPostgreEntity.getEmail());
        }
    }
}
