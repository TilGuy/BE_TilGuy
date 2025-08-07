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
        Map<String, List<Tag>> coreTagFinder = coreTagFinder(tags);
        this.keywordTagMap = convertToKeywordTagMap(subTags);
        this.tagRelationMap = convertToStringTagRelation(tagRelationMap, keywordTagMap);
        this.tagTilIdMap = convertToTagTilId(keywordTagMap, coreTagFinder);
    }

    private Map<String, List<Tag>> coreTagFinder(List<Tag> tags) {
        Map<String, List<Tag>> coreTagFinder = new HashMap<>();
        for (Tag tag : tags) {
            List<Tag> savedTags = coreTagFinder.getOrDefault(tag.getTagString(), new ArrayList<>());
            savedTags.add(tag);
            coreTagFinder.putIfAbsent(tag.getTagString(), savedTags);
        }
        return coreTagFinder;
    }

    private Map<String, List<String>> convertToKeywordTagMap(List<SubTag> subTags) {
        Map<String, List<String>> keywordTagMap = new HashMap<>();
        for (SubTag subTag : subTags) {
            if (subTag.getTag() == null) {
                continue;
            }
            String coreTagString = subTag.getTag()
                    .getTagString();
            List<String> keywordTags = keywordTagMap.getOrDefault(coreTagString, new ArrayList<>());
            keywordTags.add(subTag.getSubTagContent());
            keywordTagMap.put(coreTagString, keywordTags);
        }
        return keywordTagMap;
    }

    private Map<String, List<Long>> convertToTagTilId(
            Map<String, List<String>> keywordTagMap,
            Map<String, List<Tag>> coreTagFinder
    ) {
        Map<String, List<Long>> tagTilIds = new HashMap<>();
        Set<String> allCoreTags = keywordTagMap.keySet();

        for (String tagString : allCoreTags) {
            List<Long> tilIds = tagTilIds.getOrDefault(tagString, new ArrayList<>());
            List<Tag> tags = coreTagFinder.get(tagString);
            for (Tag tag : tags) {
                tilIds.add(tag.getTil()
                        .getTilId());
            }
            tagTilIds.putIfAbsent(tagString, tilIds);
        }

        return tagTilIds;
    }

    private Map<String, List<String>> convertToStringTagRelation(
            Map<Tag, List<Tag>> tagRelationMap,
            Map<String, List<String>> keywordTagMap
    ) {
        Map<String, List<String>> relationTags = new HashMap<>();

        for (Entry<Tag, List<Tag>> tags : tagRelationMap.entrySet()) {
            Tag key = tags.getKey();
            List<Tag> otherTags = tags.getValue();
            List<String> otherStringTags = new ArrayList<>();
            for (Tag otherTag : otherTags) {
                otherStringTags.add(otherTag.getTagString());
            }
            relationTags.put(key.getTagString(), otherStringTags);
        }

        for (String coreTag : keywordTagMap.keySet()) {
            relationTags.putIfAbsent(coreTag, new ArrayList<>());
        }

        return relationTags;
    }
}
