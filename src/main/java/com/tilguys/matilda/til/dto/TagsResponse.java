package com.tilguys.matilda.til.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import com.tilguys.matilda.til.domain.Tag;
import java.util.List;
import lombok.Getter;

@Getter
public class TagsResponse {

    @JsonValue
    private final List<String> tags;

    public TagsResponse(List<Tag> tags) {
        this.tags = tags.stream()
                .map(Tag::getTagString)
                .toList();
    }
}
