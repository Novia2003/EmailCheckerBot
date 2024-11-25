package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.entity.MailProvider;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.entity.jpa.UserJpaEntity;
import ru.tbank.emailcheckerbot.entity.redis.UserEmailRedisEntity;
import ru.tbank.emailcheckerbot.repository.jpa.UserEmailJpaRepository;
import ru.tbank.emailcheckerbot.repository.jpa.UserJpaRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserEmailJpaService {

    private static final Integer TIME_INACCURACY = 10;

    private final UserJpaRepository userJpaRepository;
    private final UserEmailJpaRepository userEmailJpaRepository;

    public void saveRefreshTokenResponse(Long id, RefreshTokenResponseDTO dto) {

        Optional<UserEmailJpaEntity> optionalUserEmailRedisEntity = userEmailJpaRepository.findById(id);
        UserEmailJpaEntity userEmailJpaEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        userEmailJpaEntity.setAccessToken(dto.getAccessToken());
        userEmailJpaEntity.setEndAccessTokenLife(calculateEndAccessTokenLife(dto.getExpiresIn()));

        if (userEmailJpaEntity.getMailProvider() == MailProvider.YANDEX) {
            userEmailJpaEntity.setRefreshToken(dto.getRefreshToken());
        }

        userEmailJpaRepository.save(userEmailJpaEntity);
    }

    private Instant calculateEndAccessTokenLife(Long expiresIn) {
        Instant currentTime = Instant.now();
        long adjustedTimeInSeconds = expiresIn - TIME_INACCURACY;

        return currentTime.plus(adjustedTimeInSeconds, ChronoUnit.SECONDS);
    }

    public String getRefreshToken(Long id) {
        Optional<UserEmailJpaEntity> optionalUserEmailRedisEntity = userEmailJpaRepository.findById(id);
        UserEmailJpaEntity userEmailJpaEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        return userEmailJpaEntity.getRefreshToken();
    }

    public MailProvider getMailProvider(Long id) {
        Optional<UserEmailJpaEntity> optionalUserEmailRedisEntity = userEmailJpaRepository.findById(id);
        UserEmailJpaEntity userEmailJpaEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        return userEmailJpaEntity.getMailProvider();
    }

    public void saveEntityFromRedis(UserEmailRedisEntity redisEntity) {
        Long userId = redisEntity.getUserId();

        UserJpaEntity user;

        if (userJpaRepository.existsById(userId)) {
            user = userJpaRepository.getReferenceById(userId);
        } else {
            user = new UserJpaEntity();
            user.setTelegramId(userId);
            user.setChatId(redisEntity.getChatId());
            userJpaRepository.save(user);
        }

        UserEmailJpaEntity userEmailJpaEntity = getUserEmailJpaEntityFromRedisEntity(redisEntity, user);
        userEmailJpaRepository.save(userEmailJpaEntity);
    }

    private static UserEmailJpaEntity getUserEmailJpaEntityFromRedisEntity(
            UserEmailRedisEntity redisEntity, UserJpaEntity user
    ) {
        UserEmailJpaEntity jpaEntity = new UserEmailJpaEntity();

        jpaEntity.setMailProvider(redisEntity.getMailProvider());
        jpaEntity.setEmail(redisEntity.getEmail());
        jpaEntity.setAccessToken(redisEntity.getAccessToken());
        jpaEntity.setRefreshToken(redisEntity.getRefreshToken());
        jpaEntity.setEndAccessTokenLife(redisEntity.getEndAccessTokenLife());
        jpaEntity.setLastMessageUID(redisEntity.getLastMessageUID());
        jpaEntity.setUser(user);

        return jpaEntity;
    }

    public List<UserEmailJpaEntity> getUserEmails() {
        return userEmailJpaRepository.findAll();
    }

    public void setLastMessageUID(Long id, Long lastMessageUID) {
        Optional<UserEmailJpaEntity> optionalUserEmailRedisEntity = userEmailJpaRepository.findById(id);
        UserEmailJpaEntity userEmailJpaEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );
        userEmailJpaEntity.setLastMessageUID(lastMessageUID);
        userEmailJpaRepository.save(userEmailJpaEntity);
    }

    public UserEmailJpaEntity getUserEmail(Long id) {
        return userEmailJpaRepository.getReferenceById(id);
    }
}
