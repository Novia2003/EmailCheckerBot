package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.entity.UserEmailEntity;
import ru.tbank.emailcheckerbot.message.EmailMessage;
import ru.tbank.emailcheckerbot.repository.UserEmailRepository;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailCheckService {

    private final UserEmailRepository userEmailRepository;

    private final ExecutorService fixedThreadPool;
    private final EmailSessionPropertiesService emailSessionPropertiesService;
    private final NotificationService notificationService;
    private final EmailSessionService emailSessionService;

    @Scheduled(fixedDelayString = "${email.check.delay}")
    public void checkEmails() {
        log.info("Starting email check process");

        List<UserEmailEntity> emails = userEmailRepository.findAll();
        for (UserEmailEntity email : emails) {
            fixedThreadPool.submit(() -> checkForNewMessages(email));
        }

        log.info("Email check process completed");
    }

    private void checkForNewMessages(UserEmailEntity userEmailEntity) {
            Properties properties = emailSessionPropertiesService.getSessionProperties(userEmailEntity.getEmailProvider());

            EmailMessage[] newMessages = emailSessionService.getNewMessages(
                    properties,
                    userEmailEntity.getEmail(),
                    userEmailEntity.getToken(),
                    userEmailEntity.getLastMessageUID()
            );

            if (newMessages.length > 0) {
                log.info("New messages found for user: {}", userEmailEntity.getEmail());
                notificationService.notifyUser(newMessages, userEmailEntity);

                Long lastMessageUID = newMessages[newMessages.length - 1].getUid();
                userEmailEntity.setLastMessageUID(lastMessageUID);
                userEmailRepository.save(userEmailEntity);
            } else {
                log.info("No new messages found for user: {}", userEmailEntity.getEmail());
            }
    }
}
