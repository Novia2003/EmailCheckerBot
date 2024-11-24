package ru.tbank.emailcheckerbot.repository.redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.emailcheckerbot.entity.redis.UserEmailRedisEntity;

@Repository
public interface UserEmailRedisRepository extends CrudRepository<UserEmailRedisEntity, Long> {
}
