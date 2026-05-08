package com.project.travel.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenRedisService {
    private final StringRedisTemplate redisTemplate;
    private final static String REFRESH_USER_PREFIX = "refresh:user:";
    private final static String BLACKLIST_PREFIX = "blacklist:";

    public void saveRefreshToken(Integer userNo, String refreshToken, long ttlMs) {
        redisTemplate.opsForValue().set(
                REFRESH_USER_PREFIX + userNo,
                refreshToken,
                Duration.ofMillis(ttlMs)
        );
    }

    public String getRefreshToken(Integer userNo) {
        return redisTemplate.opsForValue().get(REFRESH_USER_PREFIX + userNo);
    }

    public void deleteRefreshToken(Integer userNo) {
        redisTemplate.delete(REFRESH_USER_PREFIX + userNo);
    }

    public void addToBlacklist(String accessToken, long remainMs) {
        if (remainMs <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + accessToken,
                "logout",
                Duration.ofMillis(remainMs)
        );
    }

    public boolean isInBlacklist(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
}
