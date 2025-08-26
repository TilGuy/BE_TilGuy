package com.tilguys.matilda.tag.cache;

import com.tilguys.matilda.common.auth.exception.MatildaException;
import com.tilguys.matilda.tag.domain.TilTagRelations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecentTilTagsCache {

    private static final String RECENT_TAG_RELATIONS_KEY = "recent:til:relations";

    private final Cache cache;
    private TilTagRelations recentTagRelations;

    public RecentTilTagsCache(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("tilTags");
    }

    public TilTagRelations getRecentTagRelations() {
        try {
            TilTagRelations cached = cache.get(RECENT_TAG_RELATIONS_KEY, TilTagRelations.class);
            if (cached == null) {
                throw new MatildaException("캐시를 가져오는데 실패하였습니다");
            }
            return cached;
        } catch (Exception e) {
            log.error("최근 태그 정보를 가져오는데 실패하였습니다", e);
            return recentTagRelations;
        }
    }

    public void updateRecentTagRelations(TilTagRelations recentTagRelations) {
        this.recentTagRelations = recentTagRelations;
        cache.put(RECENT_TAG_RELATIONS_KEY, recentTagRelations);
    }
}
