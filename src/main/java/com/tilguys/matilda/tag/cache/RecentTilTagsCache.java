package com.tilguys.matilda.tag.cache;

import com.tilguys.matilda.common.auth.exception.MatildaException;
import com.tilguys.matilda.common.cache.RedisCacheService;
import com.tilguys.matilda.tag.domain.TilTagRelations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.logging.Logger;

@Component
public class RecentTilTagsCache {

    private static final String RECENT_TAG_RELATIONS_KEY = "recent:til:relations";
    private static final Duration RECENT_TAG_RELATIONS_TTL = Duration.ofMinutes(40);

    private static final Logger logger = Logger.getLogger(RecentTilTagsCache.class.getName());

    private final RedisCacheService redisCacheService;
    private TilTagRelations recentTagRelations;

    public RecentTilTagsCache(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    public TilTagRelations getRecentTagRelations() {
        try {
            return redisCacheService.get(RECENT_TAG_RELATIONS_KEY, TilTagRelations.class)
                    .orElseThrow(() -> new MatildaException("캐시를 가져오는데 실패하였습니다"));
        } catch (Exception e) {
            return recentTagRelations;
        }
    }

    public void updateRecentTagRelations(TilTagRelations recentTagRelations) {
        this.recentTagRelations = recentTagRelations;
        redisCacheService.set(RECENT_TAG_RELATIONS_KEY, recentTagRelations, RECENT_TAG_RELATIONS_TTL);
    }
}
