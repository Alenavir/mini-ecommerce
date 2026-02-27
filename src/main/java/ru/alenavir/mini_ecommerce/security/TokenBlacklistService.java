package ru.alenavir.mini_ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "blacklist:";

    // Добавление JTI в Redis с TTL
    public void blacklistToken(String jti, Date expiration) {
        long ttlMillis = expiration.getTime() - System.currentTimeMillis();
        if (ttlMillis > 0) {
            redisTemplate.opsForValue().set(PREFIX + jti, "revoked", Duration.ofMillis(ttlMillis));
        }
    }

    // Проверка JTI
    public boolean isBlacklisted(String jti) {
        return redisTemplate.hasKey(PREFIX + jti);
    }
}
