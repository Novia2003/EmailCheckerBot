package ru.tbank.emailcheckerbot.repository.postgre;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.emailcheckerbot.entity.postgre.UserPostgreEntity;

public interface UserPostgreRepository extends JpaRepository<UserPostgreEntity, Long> {
}
