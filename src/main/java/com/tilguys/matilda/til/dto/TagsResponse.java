package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Tag;
import java.util.List;
import lombok.Getter;

@Getter
public class TagsResponse {

    private final List<String> tags;

    public TagsResponse(List<Tag> tags) {
        this.tags = tags.stream()
                .map(Tag::getTagString)
                .toList();
    }
}
