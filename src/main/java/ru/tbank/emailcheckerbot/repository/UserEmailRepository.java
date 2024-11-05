package ru.tbank.emailcheckerbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.emailcheckerbot.entity.UserEmailEntity;

public interface UserEmailRepository extends JpaRepository<UserEmailEntity, Long> {
}
