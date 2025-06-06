package com.tilguys.matilda.tag.domain;

import com.tilguys.matilda.til.domain.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TilTags {

    private final List<Tag> tags;
    private final Map<String, Tag> tagMap = new HashMap<>();

    public TilTags(List<Tag> tags) {
        this.tags = tags;
        crateTagMap();
    }

    private void crateTagMap() {
        for (Tag tag : tags) {
            tagMap.put(tag.getTagString(), tag);
        }
    }

    public Tag findTag(String tag) {
        return tagMap.get(tag);
    }
}
