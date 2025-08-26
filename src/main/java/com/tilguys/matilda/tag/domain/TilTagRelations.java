package com.tilguys.matilda.tag.domain;

import com.tilguys.matilda.til.domain.Tag;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Getter
public class TilTagRelations {

    private final Map<String, List<String>> keywordTagMap;
    private final Map<String, List<Long>> tagTilIdMap;
    private final Map<String, List<String>> tagRelationMap;

    public TilTagRelations(List<Tag> tags, List<SubTag> subTags, Map<Tag, List<Tag>> tagRelationMap) {
        int estimatedSize = Math.max(16, tags.size() / 2);

        Map<String, List<Tag>> coreTagFinder = coreTagFinder(tags, estimatedSize);
        this.keywordTagMap = convertToKeywordTagMap(subTags, estimatedSize);
        this.tagRelationMap = convertToStringTagRelation(tagRelationMap, keywordTagMap, estimatedSize);
        this.tagTilIdMap = convertToTagTilId(keywordTagMap, coreTagFinder, estimatedSize);
    }

    private Map<String, List<Tag>> coreTagFinder(List<Tag> tags, int initialCapacity) {
        Map<String, List<Tag>> coreTagFinder = new HashMap<>(initialCapacity);
        for (Tag tag : tags) {
            String tagString = tag.getTagString();
            List<Tag> savedTags = coreTagFinder.get(tagString);
            if (savedTags == null) {
                savedTags = new ArrayList<>(4);
                coreTagFinder.put(tagString, savedTags);
            }
            savedTags.add(tag);
        }
        return coreTagFinder;
    }

    private Map<String, List<String>> convertToKeywordTagMap(List<SubTag> subTags, int initialCapacity) {
        Map<String, List<String>> keywordTagMap = new HashMap<>(initialCapacity);
        for (SubTag subTag : subTags) {
            if (subTag.getTag() == null) {
                continue;
            }
            String coreTagString = subTag.getTag()
                    .getTagString();

            List<String> keywordTags = keywordTagMap.get(coreTagString);
            if (keywordTags == null) {
                keywordTags = new ArrayList<>(4);  // 초기 용량 4로 설정
                keywordTagMap.put(coreTagString, keywordTags);
            }
            keywordTags.add(subTag.getSubTagContent());
        }
        return keywordTagMap;
    }

    private Map<String, List<Long>> convertToTagTilId(
            Map<String, List<String>> keywordTagMap,
            Map<String, List<Tag>> coreTagFinder,
            int initialCapacity
    ) {
        Map<String, List<Long>> tagTilIds = new HashMap<>(initialCapacity);
        Set<String> allCoreTags = keywordTagMap.keySet();

        for (String tagString : allCoreTags) {
            List<Tag> tags = coreTagFinder.get(tagString);
            List<Long> tilIds = new ArrayList<>(tags.size());  // 정확한 크기로 초기화

            for (Tag tag : tags) {
                tilIds.add(tag.getTil()
                        .getTilId());
            }
            tagTilIds.put(tagString, tilIds);
        }

        return tagTilIds;
    }

    private Map<String, List<String>> convertToStringTagRelation(
            Map<Tag, List<Tag>> tagRelationMap,
            Map<String, List<String>> keywordTagMap,
            int initialCapacity
    ) {
        Map<String, List<String>> relationTags = new HashMap<>(initialCapacity);

        for (Entry<Tag, List<Tag>> tags : tagRelationMap.entrySet()) {
            Tag key = tags.getKey();
            List<Tag> otherTags = tags.getValue();
            List<String> otherStringTags = new ArrayList<>(otherTags.size());  // 정확한 크기로 초기화

            for (Tag otherTag : otherTags) {
                otherStringTags.add(otherTag.getTagString());
            }
            relationTags.put(key.getTagString(), otherStringTags);
        }

        for (String coreTag : keywordTagMap.keySet()) {
            relationTags.putIfAbsent(coreTag, new ArrayList<>(0));  // 빈 리스트는 0 크기로
        }

        return relationTags;
    }
}
