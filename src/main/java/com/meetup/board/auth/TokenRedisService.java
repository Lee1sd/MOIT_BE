package com.meetup.board.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

// refresh token 저장/검증, access token 블랙리스트를 Redis로 관리.
// - refresh:{userId}      -> 현재 유효한 refresh token (재발급 시 로테이션)
// - blacklist:{token}     -> 로그아웃된 access token (만료까지만 TTL 유지하면 됨)
@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(Long userId, String refreshToken, long expirationMs) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + userId,
                refreshToken,
                Duration.ofMillis(expirationMs)
        );
    }

    public boolean isRefreshTokenValid(Long userId, String refreshToken) {
        String saved = redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
        return saved != null && saved.equals(refreshToken);
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }

    public void blacklistAccessToken(String accessToken, long remainingMs) {
        if (remainingMs <= 0) return;
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + accessToken,
                "logout",
                Duration.ofMillis(remainingMs)
        );
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
}
