package ru.tbank.emailcheckerbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.emailcheckerbot.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
