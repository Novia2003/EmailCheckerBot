package ru.tbank.emailcheckerbot.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.entity.jpa.UserJpaEntity;
import ru.tbank.emailcheckerbot.entity.redis.UserEmailRedisEntity;
import ru.tbank.emailcheckerbot.exeption.UserEmailJpaEntityNotFoundException;
import ru.tbank.emailcheckerbot.repository.jpa.UserEmailJpaRepository;
import ru.tbank.emailcheckerbot.repository.jpa.UserJpaRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserEmailJpaService {

    private static final Integer TIME_INACCURACY = 10;

    private final UserJpaRepository userJpaRepository;
    private final UserEmailJpaRepository userEmailJpaRepository;

    public void saveRefreshTokenResponse(Long id, RefreshTokenResponseDTO dto) {
        UserEmailJpaEntity userEmailJpaEntity = getUserEmailJpaEntity(id);

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
        return getUserEmailJpaEntity(id).getRefreshToken();
    }

    public MailProvider getMailProvider(Long id) {
        return getUserEmailJpaEntity(id).getMailProvider();
    }

    public void saveEntityFromRedis(UserEmailRedisEntity redisEntity) {
        Long userId = redisEntity.getUserId();

        UserJpaEntity user;

        if (userJpaRepository.existsByTelegramId(userId)) {
            user = userJpaRepository.getByTelegramId(userId);
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
        UserEmailJpaEntity userEmailJpaEntity = getUserEmailJpaEntity(id);
        userEmailJpaEntity.setLastMessageUID(lastMessageUID);
        userEmailJpaRepository.save(userEmailJpaEntity);
    }

    public boolean existsByUserIdAndEmail(Long userId, String email) {
        if (!userJpaRepository.existsByTelegramId(userId)) {
            return false;
        }

        UserJpaEntity user = userJpaRepository.getByTelegramId(userId);

        return userEmailJpaRepository.existsByUserAndEmail(user, email);
    }

    public List<UserEmailJpaEntity> getEmailsList(Long userTelegramId) {
        if (!userJpaRepository.existsByTelegramId(userTelegramId)) {
            return null;
        }

        UserJpaEntity user = userJpaRepository.getByTelegramId(userTelegramId);

        return userEmailJpaRepository.findByUser(user);
    }

    public String removeEmail(Long userEmailId) {
        try {
            UserEmailJpaEntity entity = getUserEmailJpaEntity(userEmailId);
            String email = entity.getEmail();
            userEmailJpaRepository.delete(entity);

            return email;
        } catch (UserEmailJpaEntityNotFoundException e) {
            return null;
        }
    }

    public UserEmailJpaEntity getUserEmailJpaEntity(Long id) {
        return userEmailJpaRepository.findById(id)
                .orElseThrow(() -> new UserEmailJpaEntityNotFoundException("UserEmailJpaEntity is not present"));
    }
}
