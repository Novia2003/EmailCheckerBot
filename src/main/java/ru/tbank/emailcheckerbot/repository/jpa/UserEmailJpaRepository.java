package ru.tbank.emailcheckerbot.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.emailcheckerbot.entity.jpa.UserEmailJpaEntity;
import ru.tbank.emailcheckerbot.entity.jpa.UserJpaEntity;

public interface UserEmailJpaRepository extends JpaRepository<UserEmailJpaEntity, Long> {
    boolean existsByUserAndEmail(UserJpaEntity user, String email);
}
