package com.tilguys.matilda.tag.cache;

import com.tilguys.matilda.tag.domain.TilTagRelations;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class RecentTilTagsCache {

    @Getter
    private TilTagRelations recentTagRelations;

    public RecentTilTagsCache() {
        recentTagRelations = new TilTagRelations(new ArrayList<>(), new ArrayList<>(), new HashMap<>());
    }

    public void updateRecentTagRelations(TilTagRelations recentTagRelations) {
        this.recentTagRelations = recentTagRelations;
    }
}
