package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.entity.MailProvider;
import ru.tbank.emailcheckerbot.entity.postgre.UserEmailPostgreEntity;
import ru.tbank.emailcheckerbot.message.EmailMessage;

import java.time.Instant;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailUIDService {

    private final EmailSessionPropertiesService emailSessionPropertiesService;
    private final EmailSessionService emailSessionService;
    private final UserEmailPostgreService userEmailPostgreService;
    private final AuthenticationService authenticationService;

    public long getLastMessageUID(String email, MailProvider emailProvider, String token) {
        Properties properties = emailSessionPropertiesService.getSessionProperties(emailProvider.getConfigurationName());

        return emailSessionService.getLastMessageUID(properties, email, token);
    }

    public String getMessageByUID(long userEmailId, long messageUID) {
        UserEmailPostgreEntity userEmail = userEmailPostgreService.getUserEmail(userEmailId);
        Properties properties = emailSessionPropertiesService.getSessionProperties(
                userEmail.getMailProvider().getConfigurationName()
        );

        if (Instant.now().isAfter(userEmail.getEndAccessTokenLife())) {
            authenticationService.refreshToken(userEmail.getId(), true);
        }

        EmailMessage message = emailSessionService.getMessageByUID(
                properties,
                userEmail.getEmail(),
                userEmail.getAccessToken(),
                messageUID
        );

        return message.getContent();
    }
}
