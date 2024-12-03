package ru.tbank.emailcheckerbot.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.emailcheckerbot.entity.jpa.UserJpaEntity;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    boolean existsByTelegramId(Long telegramId);

    UserJpaEntity getByTelegramId(Long telegramId);
}
