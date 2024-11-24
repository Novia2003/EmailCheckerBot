package ru.tbank.emailcheckerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.emailcheckerbot.entity.MailProvider;
import ru.tbank.emailcheckerbot.dto.token.RefreshTokenResponseDTO;
import ru.tbank.emailcheckerbot.entity.postgre.UserEmailPostgreEntity;
import ru.tbank.emailcheckerbot.entity.postgre.UserPostgreEntity;
import ru.tbank.emailcheckerbot.entity.redis.UserEmailRedisEntity;
import ru.tbank.emailcheckerbot.repository.postgre.UserEmailPostgreRepository;
import ru.tbank.emailcheckerbot.repository.postgre.UserPostgreRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserEmailPostgreService {

    private static final Integer TIME_INACCURACY = 10;

    private final UserPostgreRepository userPostgreRepository;
    private final UserEmailPostgreRepository userEmailPostgreRepository;

    public void saveRefreshTokenResponse(Long id, RefreshTokenResponseDTO dto) {

        Optional<UserEmailPostgreEntity> optionalUserEmailRedisEntity = userEmailPostgreRepository.findById(id);
        UserEmailPostgreEntity userEmailPostgreEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        userEmailPostgreEntity.setAccessToken(dto.getAccessToken());
        userEmailPostgreEntity.setEndAccessTokenLife(calculateEndAccessTokenLife(dto.getExpiresIn()));

        if (userEmailPostgreEntity.getMailProvider() == MailProvider.YANDEX) {
            userEmailPostgreEntity.setRefreshToken(dto.getRefreshToken());
        }

        userEmailPostgreRepository.save(userEmailPostgreEntity);
    }

    private Instant calculateEndAccessTokenLife(Long expiresIn) {
        Instant currentTime = Instant.now();
        long adjustedTimeInSeconds = expiresIn - TIME_INACCURACY;

        return currentTime.plus(adjustedTimeInSeconds, ChronoUnit.SECONDS);
    }

    public String getRefreshToken(Long id) {
        Optional<UserEmailPostgreEntity> optionalUserEmailRedisEntity = userEmailPostgreRepository.findById(id);
        UserEmailPostgreEntity userEmailPostgreEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        return userEmailPostgreEntity.getRefreshToken();
    }

    public MailProvider getMailProvider(Long id) {
        Optional<UserEmailPostgreEntity> optionalUserEmailRedisEntity = userEmailPostgreRepository.findById(id);
        UserEmailPostgreEntity userEmailPostgreEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );

        return userEmailPostgreEntity.getMailProvider();
    }

    public void saveEntityFromRedis(UserEmailRedisEntity redisEntity) {
        Long userId = redisEntity.getUserId();

        UserPostgreEntity user;

        if (userPostgreRepository.existsById(userId)) {
            user = userPostgreRepository.getReferenceById(userId);
        } else {
            user = new UserPostgreEntity();
            user.setTelegramId(userId);
            user.setChatId(redisEntity.getChatId());
            userPostgreRepository.save(user);
        }

        UserEmailPostgreEntity userEmailPostgreEntity = getUserEmailPostgreEntityFromRedisEntity(redisEntity, user);
        userEmailPostgreRepository.save(userEmailPostgreEntity);
    }

    private static UserEmailPostgreEntity getUserEmailPostgreEntityFromRedisEntity(
            UserEmailRedisEntity redisEntity, UserPostgreEntity user
    ) {
        UserEmailPostgreEntity postgreEntity = new UserEmailPostgreEntity();

        postgreEntity.setMailProvider(redisEntity.getMailProvider());
        postgreEntity.setEmail(redisEntity.getEmail());
        postgreEntity.setAccessToken(redisEntity.getAccessToken());
        postgreEntity.setRefreshToken(redisEntity.getRefreshToken());
        postgreEntity.setEndAccessTokenLife(redisEntity.getEndAccessTokenLife());
        postgreEntity.setLastMessageUID(redisEntity.getLastMessageUID());
        postgreEntity.setUser(user);

        return postgreEntity;
    }

    public List<UserEmailPostgreEntity> getUserEmails() {
        return userEmailPostgreRepository.findAll();
    }

    public void setLastMessageUID(Long id, Long lastMessageUID) {
        Optional<UserEmailPostgreEntity> optionalUserEmailRedisEntity = userEmailPostgreRepository.findById(id);
        UserEmailPostgreEntity userEmailPostgreEntity = optionalUserEmailRedisEntity.orElseThrow(
                () -> new RuntimeException("UserEmailRedisEntity is not present")
        );
        userEmailPostgreEntity.setLastMessageUID(lastMessageUID);
        userEmailPostgreRepository.save(userEmailPostgreEntity);
    }

    public UserEmailPostgreEntity getUserEmail(Long id) {
        return userEmailPostgreRepository.getReferenceById(id);
    }
}
