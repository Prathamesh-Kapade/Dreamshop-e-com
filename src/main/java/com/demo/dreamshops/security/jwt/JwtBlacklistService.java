package com.demo.dreamshops.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "blacklist:";

    public void blacklistToken(String token, long expiration) {
        redisTemplate.opsForValue().set(PREFIX + token, "true", expiration, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }
}