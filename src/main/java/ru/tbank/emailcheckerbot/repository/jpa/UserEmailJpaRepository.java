package ru.tbank.emailcheckerbot.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;

public interface UserEmailJpaRepository extends JpaRepository<UserEmailJpaEntity, Long> {
}
