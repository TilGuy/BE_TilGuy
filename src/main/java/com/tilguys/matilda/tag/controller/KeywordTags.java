package com.tilguys.matilda.tag.controller;

import com.tilguys.matilda.tag.domain.SubTag;
import com.tilguys.matilda.til.domain.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class KeywordTags {

    private final Map<String, List<String>> keywordTagMap = new HashMap<>();
    private final Map<String, List<Long>> tagTilIdMap;
    private final Map<String, List<String>> tagRelationMap;

    public KeywordTags(List<Tag> tags, List<SubTag> subTags, Map<Tag, List<Tag>> tagRelationMap) {
        initiateCoreTag(tags);

        for (SubTag subTag : subTags) {
            if (subTag.getTag() == null) {
                continue;
            }
            String coreTagString = subTag.getTag().getTagString();
            List<String> keywordTags = keywordTagMap.getOrDefault(coreTagString, new ArrayList<>());
            keywordTags.add(subTag.getSubTag());
            keywordTagMap.put(coreTagString, keywordTags);
        }
        this.tagRelationMap = convertToStringTagRelation(tagRelationMap);
        this.tagTilIdMap = convertToTagTilId(tagRelationMap);
    }

    private Map<String, List<Long>> convertToTagTilId(Map<Tag, List<Tag>> tagRelationMap) {
        Map<String, List<Long>> tagTilIds = new HashMap<>();
        Set<Tag> allCoreTags = getAllCoreTags(tagRelationMap);

        for (Tag tag : allCoreTags) {
            List<Long> tilIds = tagTilIds.getOrDefault(tag.getTagString(), new ArrayList<>());
            tilIds.add(tag.getTil().getTilId());
            tagTilIds.put(tag.getTagString(), tilIds);
        }

        return tagTilIds;
    }

    private Set<Tag> getAllCoreTags(Map<Tag, List<Tag>> tagRelationMap) {
        Set<Tag> allCoreTags = tagRelationMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        allCoreTags.addAll(tagRelationMap.keySet());
        return allCoreTags;
    }

    private Map<String, List<String>> convertToStringTagRelation(Map<Tag, List<Tag>> tagRelationMap) {
        Map<String, List<String>> relationTags = new HashMap<>();

        for (Tag tag : tagRelationMap.keySet()) {
            List<Tag> otherTags = tagRelationMap.get(tag);
            List<String> otherStringTags = new ArrayList<>();
            for (Tag otherTag : otherTags) {
                otherStringTags.add(otherTag.getTagString());
            }
            relationTags.put(tag.getTagString(), otherStringTags);
        }
        return relationTags;
    }

    private void initiateCoreTag(List<Tag> tags) {
        for (Tag tag : tags) {
            keywordTagMap.put(tag.getTagString(), new ArrayList<>());
        }
    }
}
