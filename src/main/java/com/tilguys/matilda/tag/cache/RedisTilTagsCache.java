package com.tilguys.matilda.tag.cache;

import com.tilguys.matilda.common.cache.RedisCacheService;
import com.tilguys.matilda.tag.domain.TilTags;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisTilTagsCache {

    private static final String RECENT_TAGS_KEY = "recent:til:tags";
    private static final String TAGS_BY_CONTENT_KEY = "tags:content:";
    private static final String USER_TAGS_KEY = "tags:user:";
    private static final Duration RECENT_TAGS_TTL = Duration.ofMinutes(10);
    private static final Duration CONTENT_TAGS_TTL = Duration.ofMinutes(15);
    private static final Duration USER_TAGS_TTL = Duration.ofMinutes(30);
    private final RedisCacheService redisCacheService;

    public void cacheRecentTags(List<String> tags) {
        redisCacheService.set(RECENT_TAGS_KEY, tags, RECENT_TAGS_TTL);
    }

    @SuppressWarnings("unchecked")
    public Optional<List<String>> getRecentTags() {
        return (Optional<List<String>>) (Optional<?>) redisCacheService.get(RECENT_TAGS_KEY, List.class);
    }

    public void cacheTagsByContent(String contentHash, TilTags tags) {
        String key = TAGS_BY_CONTENT_KEY + contentHash;
        redisCacheService.set(key, tags, CONTENT_TAGS_TTL);
    }

    public Optional<TilTags> getTagsByContent(String contentHash) {
        String key = TAGS_BY_CONTENT_KEY + contentHash;
        return redisCacheService.get(key, TilTags.class);
    }

    public void cacheUserTags(Long userId, List<String> tags) {
        String key = USER_TAGS_KEY + userId;
        redisCacheService.set(key, tags, USER_TAGS_TTL);
    }

    @SuppressWarnings("unchecked")
    public Optional<List<String>> getUserTags(Long userId) {
        String key = USER_TAGS_KEY + userId;
        return (Optional<List<String>>) (Optional<?>) redisCacheService.get(key, List.class);
    }

    public void invalidateUserTags(Long userId) {
        String key = USER_TAGS_KEY + userId;
        redisCacheService.delete(key);
    }

    public void invalidateContentTags(String contentHash) {
        String key = TAGS_BY_CONTENT_KEY + contentHash;
        redisCacheService.delete(key);
    }

    public void invalidateAllTags() {
        redisCacheService.deletePattern("tags:*");
        redisCacheService.deletePattern("recent:*");
    }
}


