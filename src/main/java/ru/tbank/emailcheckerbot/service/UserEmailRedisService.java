package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.tbank.emailcheckerbot.entity.MailProvider;
import ru.tbank.emailcheckerbot.dto.token.AccessTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.entity.redis.UserEmailRedisEntity;
import ru.tbank.emailcheckerbot.repository.redis.UserEmailRedisRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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
        Optional<UserEmailRedisEntity> optionalUserEmailRedisEntity = userEmailRedisRepository.findById(userId);
        UserEmailRedisEntity userEmailRedisEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        userEmailRedisEntity.setAccessToken(dto.getAccessToken());
        userEmailRedisEntity.setRefreshToken(dto.getRefreshToken());
        userEmailRedisEntity.setEndAccessTokenLife(calculateEndAccessTokenLife(dto.getExpiresIn()));
        userEmailRedisEntity.setMailProvider(mailProvider);
        userEmailRedisEntity.setEmail(email);

        userEmailRedisRepository.save(userEmailRedisEntity);
    }

    public void saveRefreshTokenResponse(Long id, RefreshTokenResponseDTO dto) {
        Optional<UserEmailRedisEntity> optionalUserEmailRedisEntity = userEmailRedisRepository.findById(id);
        UserEmailRedisEntity userEmailRedisEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

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
        Optional<UserEmailRedisEntity> optionalUserEmailRedisEntity = userEmailRedisRepository.findById(userId);
        UserEmailRedisEntity userEmailRedisEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        return userEmailRedisEntity.getAccessToken();
    }

    public String getRefreshToken(Long userId) {
        Optional<UserEmailRedisEntity> optionalUserEmailRedisEntity = userEmailRedisRepository.findById(userId);
        UserEmailRedisEntity userEmailRedisEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        return userEmailRedisEntity.getRefreshToken();
    }

    public MailProvider getMailProvider(Long userId) {
        Optional<UserEmailRedisEntity> optionalUserEmailRedisEntity = userEmailRedisRepository.findById(userId);
        UserEmailRedisEntity userEmailRedisEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        return userEmailRedisEntity.getMailProvider();
    }

    public Instant getEndAccessTokenLife(Long userId) {
        Optional<UserEmailRedisEntity> optionalUserEmailRedisEntity = userEmailRedisRepository.findById(userId);
        UserEmailRedisEntity userEmailRedisEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        return userEmailRedisEntity.getEndAccessTokenLife();
    }

    public String getEmail(Long userId) {
        Optional<UserEmailRedisEntity> optionalUserEmailRedisEntity = userEmailRedisRepository.findById(userId);
        UserEmailRedisEntity userEmailRedisEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        return userEmailRedisEntity.getEmail();
    }

    public void setLastMessageUID(Long userId, Long lastMessageUID) {
        Optional<UserEmailRedisEntity> optionalUserEmailRedisEntity = userEmailRedisRepository.findById(userId);
        UserEmailRedisEntity userEmailRedisEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        userEmailRedisEntity.setLastMessageUID(lastMessageUID);
        userEmailRedisRepository.save(userEmailRedisEntity);
    }

    public void transferEntityFromRedisToJpa(Long userId) {
        Optional<UserEmailRedisEntity> optionalUserEmailRedisEntity = userEmailRedisRepository.findById(userId);
        UserEmailRedisEntity userEmailRedisEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        userEmailJpaService.saveEntityFromRedis(userEmailRedisEntity);

        userEmailRedisRepository.delete(userEmailRedisEntity);
    }
}
