package ru.tbank.emailcheckerbot.entity.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import ru.tbank.emailcheckerbot.dto.type.MailProvider;

import java.time.Instant;

@Getter
@Setter
@RedisHash(timeToLive = 3600L)
public class UserEmailRedisEntity {
    @Id
    private Long userId;

    private Long chatId;

    private MailProvider mailProvider;

    private String email;

    private String accessToken;

    private String refreshToken;

    private Instant endAccessTokenLife;

    private Long lastMessageUID;
}
