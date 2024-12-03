package ru.tbank.emailcheckerbot.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.entity.redis.UserEmailRedisEntity;
import ru.tbank.emailcheckerbot.exeption.UserEmailRedisEntityNotFoundException;
import ru.tbank.emailcheckerbot.repository.redis.UserEmailRedisRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class UserEmailRedisService {

    private static final Integer TIME_INACCURACY = 10;

    private final UserEmailRedisRepository userEmailRedisRepository;

    private final UserEmailJpaService userEmailJpaService;

    public void createUserEmailRedisEntity(Long userId, Long chatId) {
        UserEmailRedisEntity userEmailRedisEntity = new UserEmailRedisEntity();
        userEmailRedisEntity.setUserId(userId);
        userEmailRedisEntity.setChatId(chatId);

        userEmailRedisRepository.save(userEmailRedisEntity);
    }

    public void saveAccessTokenResponseAndEmail(
            Long userId,
            AccessTokenResponseDTO dto,
            MailProvider mailProvider,
            String email
    ) {
        UserEmailRedisEntity userEmailRedisEntity = getUserEmailRedisEntity(userId);

        userEmailRedisEntity.setAccessToken(dto.getAccessToken());
        userEmailRedisEntity.setRefreshToken(dto.getRefreshToken());
        userEmailRedisEntity.setEndAccessTokenLife(calculateEndAccessTokenLife(dto.getExpiresIn()));
        userEmailRedisEntity.setMailProvider(mailProvider);
        userEmailRedisEntity.setEmail(email);

        userEmailRedisRepository.save(userEmailRedisEntity);
    }

    public void saveRefreshTokenResponse(Long id, RefreshTokenResponseDTO dto) {
        UserEmailRedisEntity userEmailRedisEntity = getUserEmailRedisEntity(id);

        userEmailRedisEntity.setAccessToken(dto.getAccessToken());
        userEmailRedisEntity.setEndAccessTokenLife(calculateEndAccessTokenLife(dto.getExpiresIn()));

        if (userEmailRedisEntity.getMailProvider() == MailProvider.YANDEX) {
            userEmailRedisEntity.setRefreshToken(dto.getRefreshToken());
        }

        userEmailRedisRepository.save(userEmailRedisEntity);
    }

    private Instant calculateEndAccessTokenLife(Long expiresIn) {
        Instant currentTime = Instant.now();
        long adjustedTimeInSeconds = expiresIn - TIME_INACCURACY;

        return currentTime.plus(adjustedTimeInSeconds, ChronoUnit.SECONDS);
    }

    public boolean isUserEmailRecordNotExists(Long userId) {
        return userEmailRedisRepository.findById(userId).isEmpty();
    }

    public String getAccessToken(Long userId) {
        return getUserEmailRedisEntity(userId).getAccessToken();
    }

    public String getRefreshToken(Long userId) {
        return getUserEmailRedisEntity(userId).getRefreshToken();
    }

    public MailProvider getMailProvider(Long userId) {
        return getUserEmailRedisEntity(userId).getMailProvider();
    }

    public Instant getEndAccessTokenLife(Long userId) {
        return getUserEmailRedisEntity(userId).getEndAccessTokenLife();
    }

    public UserEmailRedisEntity getUserEmailRedisEntity(Long userId) {
        return userEmailRedisRepository.findById(userId)
                .orElseThrow(() -> new UserEmailRedisEntityNotFoundException("UserEmailRedisEntity is not present"));
    }

    public void setLastMessageUID(Long userId, Long lastMessageUID) {
        UserEmailRedisEntity userEmailRedisEntity = getUserEmailRedisEntity(userId);

        userEmailRedisEntity.setLastMessageUID(lastMessageUID);
        userEmailRedisRepository.save(userEmailRedisEntity);
    }

    public void transferEntityFromRedisToJpa(Long userId) {
        UserEmailRedisEntity userEmailRedisEntity = getUserEmailRedisEntity(userId);

        userEmailJpaService.saveEntityFromRedis(userEmailRedisEntity);
        userEmailRedisRepository.delete(userEmailRedisEntity);
    }

    public void deleteEntity(Long userId) {
        UserEmailRedisEntity userEmailRedisEntity = getUserEmailRedisEntity(userId);

        userEmailRedisRepository.delete(userEmailRedisEntity);
    }
}
