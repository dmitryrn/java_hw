package com.example.mediaportal.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AnalyticsService {

    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");

    private final StringRedisTemplate redisTemplate;

    public AnalyticsService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long registerView(UUID contentId) {
        String contentKey = totalViewsKey(contentId);
        Long totalViews = redisTemplate.opsForValue().increment(contentKey);

        String rankingKey = hourlyRankingKey();
        redisTemplate.opsForZSet().incrementScore(rankingKey, contentId.toString(), 1);
        redisTemplate.expire(rankingKey, Duration.ofHours(2));

        return totalViews == null ? 0 : totalViews;
    }

    public Map<UUID, Long> topContent(int limit) {
        var entries = redisTemplate.opsForZSet()
                .reverseRangeWithScores(hourlyRankingKey(), 0, limit - 1);
        Map<UUID, Long> result = new LinkedHashMap<>();

        if (entries == null) {
            return result;
        }

        for (var entry : entries) {
            try {
                UUID id = UUID.fromString(entry.getValue());
                long views = entry.getScore() == null ? 0 : entry.getScore().longValue();
                result.put(id, views);
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed analytics members instead of breaking the endpoint.
            }
        }
        return result;
    }

    private String hourlyRankingKey() {
        return "content:popular:" + ZonedDateTime.now(ZoneOffset.UTC).format(HOUR_FORMAT);
    }

    private String totalViewsKey(UUID contentId) {
        return "content:views:total:" + contentId;
    }
}
