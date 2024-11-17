package ru.tbank.emailcheckerbot.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.tbank.emailcheckerbot.bot.command.registration.RegistrationStep;
import ru.tbank.emailcheckerbot.entity.UserEmailEntity;
import ru.tbank.emailcheckerbot.entity.UserEntity;
import ru.tbank.emailcheckerbot.repository.UserEmailRepository;
import ru.tbank.emailcheckerbot.repository.UserRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserStateService {

    private final UserEmailRepository userEmailRepository;
    private final UserRepository userRepository;

    private final Map<Long, UserEmailEntry> map = new HashMap<>();

    public RegistrationStep getStep(Long userId) {
        if (map.containsKey(userId)) {
            return map.get(userId).registrationStep;
        }

        return RegistrationStep.NONE;
    }

    public void createUserEmailEntry(Long userId, Long chatId) {
        UserEmailEntry userEmailEntity = new UserEmailEntry();
        userEmailEntity.setChatId(chatId);
        userEmailEntity.setRegistrationStep(RegistrationStep.NONE);

        map.put(userId, userEmailEntity);
    }

    public String getEmailProvider(Long userId) {
        return map.get(userId).getEmailProvider();
    }

    public void setEmailProvider(Long userId, String emailProvider) {
        map.get(userId).setEmailProvider(emailProvider);
    }

    public String getEmail(Long userId) {
        return map.get(userId).getEmail();
    }

    public void setEmail(Long userId, String email) {
        map.get(userId).setEmail(email);
    }

    public String getAccessToken(Long userId) {
        return map.get(userId).getAccessToken();
    }

    public void setAccessToken(Long userId, String token) {
        map.get(userId).setAccessToken(token);
    }

    public void setLastMessageUID(Long userId, Long lastMessageUID) {
        map.get(userId).setLastMessageUID(lastMessageUID);
    }

    public void setStep(Long userId, RegistrationStep step) {
        map.get(userId).setRegistrationStep(step);
    }

    public void clearEntry(Long userId) {
        map.remove(userId);
    }

    public void transferInformationDatabase(Long userId) {
        UserEmailEntry entry = map.get(userId);

        UserEntity user;
        if (userRepository.existsById(userId)) {
            user = userRepository.getReferenceById(userId);
        } else {
            user = new UserEntity();
            user.setTelegramId(userId);
            user.setChatId(entry.getChatId());
            userRepository.save(user);
        }

        UserEmailEntity userEmailEntity = new UserEmailEntity();
        userEmailEntity.setEmailProvider(entry.getEmailProvider());
        userEmailEntity.setEmail(entry.getEmail());
        userEmailEntity.setAccessToken(entry.getAccessToken());
        userEmailEntity.setRefreshToken(entry.getRefreshToken());
        userEmailEntity.setEndAccessTokenLife(entry.getEndAccessTokenLife());
        userEmailEntity.setLastMessageUID(entry.getLastMessageUID());
        userEmailEntity.setUser(user);
        userEmailRepository.save(userEmailEntity);

        clearEntry(userId);
    }

    @Data
    private static class UserEmailEntry {
        private long chatId;
        private String emailProvider;
        private String email;
        private String accessToken;
        private String refreshToken;
        private Instant endAccessTokenLife;
        private Long lastMessageUID;
        private RegistrationStep registrationStep;
    }
}
