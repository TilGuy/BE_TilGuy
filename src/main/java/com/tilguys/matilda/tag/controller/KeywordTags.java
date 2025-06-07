package com.tilguys.matilda.tag.controller;

import com.tilguys.matilda.tag.domain.SubTag;
import com.tilguys.matilda.til.domain.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class KeywordTags {
    private final Map<String, List<String>> keywordTagMap = new HashMap<>();
    private final Map<String, List<String>> tagRelationMap;

    public KeywordTags(List<Tag> tags, List<SubTag> subTags, Map<Tag, List<Tag>> tagRelationMap) {
        initiateCoreTag(tags);

        for (SubTag subTag : subTags) {
            String coreTagString = subTag.getTag().getTagString();
            List<String> keywordTags = keywordTagMap.getOrDefault(coreTagString, new ArrayList<>());
            keywordTags.add(subTag.getSubTag());
            keywordTagMap.put(coreTagString, keywordTags);
        }
        this.tagRelationMap = convertToStringTagRelation(tagRelationMap);
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
