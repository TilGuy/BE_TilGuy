package com.tilguys.matilda.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.error("Redis set failed: key={}, ttl={}", key, ttl, e);
        }
    }

    public void set(String key, Object value) {
        set(key, value, Duration.ofMinutes(30));
    }

    public Optional<Object> get(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        } catch (Exception e) {
            log.error("Redis get failed: key={}", key, e);
            return Optional.empty();
        }
    }

    public <T> Optional<T> get(String key, Class<T> clazz) {
        return get(key).map(clazz::cast);
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis delete failed: key={}", key, e);
        }
    }

    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Redis deletePattern failed: pattern={}", pattern, e);
        }
    }

    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis exists check failed: key={}", key, e);
            return false;
        }
    }

    public void expire(String key, Duration ttl) {
        try {
            redisTemplate.expire(key, ttl);
        } catch (Exception e) {
            log.error("Redis expire failed: key={}, ttl={}"
                    , key, ttl, e);
        }
    }
}


