package ru.tbank.emailcheckerbot.repository.postgre;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tbank.emailcheckerbot.entity.postgre.UserEmailPostgreEntity;

public interface UserEmailPostgreRepository extends JpaRepository<UserEmailPostgreEntity, Long> {
}
