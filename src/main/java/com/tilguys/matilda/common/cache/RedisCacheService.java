package com.tilguys.matilda.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue()
                    .set(key, value, ttl);
        } catch (Exception e) {
            log.error("Redis set failed: key={}, ttl={}", key, ttl, e);
        }
    }

    public void set(String key, Object value) {
        set(key, value, Duration.ofMinutes(30));
    }

    public Optional<Object> get(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue()
                    .get(key));
        } catch (Exception e) {
            log.error("Redis get failed: key={}", key, e);
            return Optional.empty();
        }
    }

    public <T> Optional<T> get(String key, Class<T> clazz) {
        return get(key).map(clazz::cast);
    }

}


